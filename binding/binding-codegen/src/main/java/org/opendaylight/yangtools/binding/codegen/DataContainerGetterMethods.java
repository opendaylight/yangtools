/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NSEE;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;

import com.google.common.base.CharMatcher;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.JavaPackageName;
import org.opendaylight.yangtools.binding.model.OverrideAnnotation;
import org.opendaylight.yangtools.binding.model.RoutingContextAnnotation;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ReturnTypeCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link BlockFragment} emitting getter methods for a {@link DataContainerArchetype}.
 */
@NonNullByDefault
final class DataContainerGetterMethods implements BlockFragment {
    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");
    private static final JavaPackageName EMPTY_PKG = JavaPackageName.of("");

    private final InterfaceTemplate<?> template;

    DataContainerGetterMethods(final InterfaceTemplate<?> template) {
        this.template = requireNonNull(template);
    }

    private String importedName(final TypeName type) {
        return template.importedName(type);
    }

    private String importedType(final GetterShape getter) {
        return template.importedName(getter.type());
    }

    private String importedNonNull(final GetterShape getter) {
        return template.importedNonNull(getter.type());
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        final var it = template.getters.methods().iterator();
        while (true) {
            final var getter = it.next();
            final var method = getter.method();
            final var statement = method.statement();

//            final var override = getter.hasOverride() ? (BlockFragment) bbb -> {
//                bbb.at().eol(template.importedName(OVERRIDE));
//            } :  null;

            final var deprecated = DeprecatedAnnotation.of(template.javaType(), statement);

            // getFoo()
            bb
                .txt(accessorJavadoc(getter, ", or {@code null} if it is not present"))
                .frg(generateAnnotations(method))
//                .frg(override)
                .frg(deprecated)
                .str(nullableType(getter)).sp().str(getter.name()).eol("();");

            switch (statement) {
                case ContainerEffectiveStatement stmt when stmt.presenceStatement() == null ->
                    // an abstract nonnullFoo()
                    bb
                        .nl()
                        .txt(accessorJavadoc(getter, ", or an empty instance if it is not present"))
                        .frg(generateAnnotations(method))
//                        .frg(override)
                        .frg(deprecated)
                        .str(importedNonNull(getter)).str(" " + NONNULL_PREFIX).str(getter.suffix()).eol("();");
                case ListEffectiveStatement stmt -> {
                    // a default nonnullFoo()
                    bb
                        .nl()
                        .txt(accessorJavadoc(getter, ", or an empty list if it is not present"))
                        .frg(generateAnnotations(method))
//                        .frg(override)
                        .frg(deprecated)
                        .str("default ").str(importedNonNull(getter)).str(" " + NONNULL_PREFIX).str(getter.suffix())
                            .str("()").oB()
                            .str("return ").str(importedName(CODEHELPERS)).str(".nonnull(").str(getter.name())
                                .eol("());")
                        .cB();
                }
                // a default requireFoo
                case AnydataEffectiveStatement stmt -> generateRequireMethod(bb, getter, null, deprecated);
                case AnyxmlEffectiveStatement stmt -> generateRequireMethod(bb, getter, null, deprecated);
                case LeafEffectiveStatement stmt -> generateRequireMethod(bb, getter, null, deprecated);
                case LeafListEffectiveStatement stmt -> generateRequireMethod(bb, getter, null, deprecated);
                default -> {
                    // nothing else
                }
            }

            if (!it.hasNext()) {
                break;
            }
            bb.newLine();
        }
    }

    private void generateRequireMethod(final BlockBuilder bb, final GetterShape getter,
            final @Nullable BlockFragment override, final @Nullable DeprecatedAnnotation deprecated) {
        // FIXME: remove this bail out
        if (getter.hasOverride()) {
            return;
        }

        bb
            .nl()
            .txt(accessorJavadoc(getter, ", guaranteed to be non-null", NSEE))
            .frg(override)
            .frg(deprecated)
            .str("default ").str(importedNonNull(getter)).str(" " + REQUIRE_PREFIX).str(getter.suffix()).str("()").oB()
                .str("return ").str(importedName(CODEHELPERS)).str(".require(").str(getter.name())
                    .str("(), ").jStr(getter.propName()).eol(");")
            .cB();
    }

    // FIXME: return a Block
    private String accessorJavadoc(final GetterShape getter, final String orString) {
        return accessorJavadoc(getter, orString, null);
    }

    // FIXME: return a Block
    private String accessorJavadoc(final GetterShape getter, final String orString,
            final @Nullable TypeName exception) {
        final var optDescription = getter.method().statement()
            .findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        if (optDescription.isEmpty()) {
            return simpleAccessorJavadoc(getter, orString, exception);
        }

        final var reference = optDescription.orElseThrow();
        final var propName = getter.propName();
        final var bb = template.newBlockBuilder()
            .str("Return ").str(propName).str(orString).eol(".")
            .blk(formatReference(reference))
            .nl()
            .str("@return {@code ").str(importedType(getter)).str("} ").str(propName).str(orString).eol(".");
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).eol(" is not present");
        }
        return bb.toJavadocBlock();
    }

    // FIXME: return a Block
    private String simpleAccessorJavadoc(final GetterShape getter, final String orString,
            final @Nullable TypeName exception) {
        final var propName = getter.propName();

        final var bb = template.newBlockBuilder()
            .str("{@return {@code ").str(importedType(getter)).str("} ").str(propName).str(orString).eol("}");
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).eol(" is not present");
        }
        return bb.toJavadocBlock();
    }

    private static BlockBuilder formatReference(final String reference) {
        final var bb = Block.builder()
            .txt("""
                <pre>
                    <code>
                """);

        // FIXME: use a {@code} block which will render some of this encoding superfluous, but it requires paying
        //        attention to '}' pairing in input
        var formattedText = DocUtils.encodeAngleBrackets(reference);
        formattedText = WS_MATCHER.replaceFrom(JavaFileTemplate.encodeJavadocSymbols(formattedText), ' ');
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ");

        // FIXME: add state keeping so that we can append direcly to BlockBuilder
        var sb = new StringBuilder();
        var isFirstElementOnNewLineEmptyChar = false;

        // FIXME: use indexOf(' ') instead of StringTokenizer
        final var tokenizer = new StringTokenizer(formattedText, " ", true);
        while (tokenizer.hasMoreTokens()) {
            final var nextElement = tokenizer.nextToken();
            final var lbLength = sb.length();

            if (lbLength != 0 && lbLength + nextElement.length() > 80) {
                final var limit = lbLength - 1;
                if (sb.charAt(limit) == ' ') {
                    sb.setLength(limit);
                }
                // FIXME: use append(CharSequence, int, int) instead
                if (!sb.isEmpty() && sb.charAt(0) == ' ') {
                    sb.deleteCharAt(0);
                }
                bb.str("        ").eol(sb.toString());
                sb.setLength(0);

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            } else {
                sb.append(nextElement);
            }
        }
        if (!sb.isEmpty()) {
            bb.str("        ").eol(sb.toString());
        }

        return bb
            .txt("""
                    </code>
                </pre>

                """);
    }

    private String nullableType(final GetterShape getter) {
        final var type = getter.type();
        if (isObject(type) && type instanceof ReturnTypeCompat) {
            return template.importedNullable(type);
        }
        return template.importedName(type);
    }

    // The return type has a package, so it's not a primitive type
    private static boolean isObject(final Type type) {
        return !EMPTY_PKG.equals(type.packageName());
    }

    private @Nullable BlockFragment generateAnnotations(final GetterMethod getter) {
        final var annotations = getter.annotations();
        if (annotations.isEmpty()) {
            return null;
        }

        return bb -> {
            for (var annotation : annotations) {
                bb.at().str(importedName(annotation.type()));
                switch (annotation) {
                    case RoutingContextAnnotation routingContext -> {
                        bb.str("(value = ").str(template.importedName(routingContext.value())).str(".class)");
                    }
                    case OverrideAnnotation unused -> {
                        // no-op
                    }
                }
                bb.newLine();
            }
        };
    }
}
