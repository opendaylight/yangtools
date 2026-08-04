/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.JavaFileTemplate.propertyNameFromGetter;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NSEE;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;

import com.google.common.base.CharMatcher;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.OverrideAnnotation;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link BlockFragment} emitting getter methods for a {@link DataContainerArchetype}.
 */
@NonNullByDefault
record DataContainerGetters(InterfaceTemplate<?> template) implements BlockFragment {
    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

    DataContainerGetters {
        requireNonNull(template);
    }

    private String importedName(final JavaTypeName type) {
        return template.importedName(type);
    }

    private String importedType(final MethodSignature method) {
        return template.importedName(method.returnType());
    }

    private String importedNonNull(final Type type) {
        return template.importedNonNull(type);
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        final var it = template.archetype.getMethodDefinitions().iterator();
        verify(it.hasNext());

        while (true) {
            final var method = it.next();

            // FIXME: refactor this:
            // - generate getter while looking at the statement and:
            //   - if it is a list or a non-presence container
            //     - generate the getFoo as NULLIFY_EMPTY
            //     - generate a nonnullFoo returning empty for non-existent
            //   - if it is a leaf or leaf-list:
            //     - generate a requireFoo
            final var methodName = method.name();

            // ignore methods
            if (!isGetterMethodName(methodName)) {
                if (it.hasNext()) {
                    continue;
                }
                break;
            }

            // getFoo()
            bb
                .txt(accessorJavadoc(method, ", or {@code null} if it is not present"))
                .frg(generateAccessorAnnotations(method))
                .str(nullableType(method.returnType())).sp().str(method.name()).eol("();");

            switch (method.statement()) {
                case ContainerEffectiveStatement stmt when stmt.presenceStatement() == null ->
                    // an abstract nonnullFoo()
                    bb
                        .nl()
                        .txt(accessorJavadoc(method, ", or an empty instance if it is not present"))
                        .frg(generateAnnotations(method))
                        .str(importedNonNull(method.returnType())).str(" " + NONNULL_PREFIX).str(method.name()
                            .substring(3)).eol("();");
                case ListEffectiveStatement stmt -> {
                    // a default nonnullFoo()
                    final var getterName = method.name();
                    final var stem = getterName.substring(3);

                    bb
                        .nl()
                        .txt(accessorJavadoc(method, ", or an empty list if it is not present"))
                        .frg(generateAnnotations(method))
                        .str("default ").str(importedNonNull(method.returnType())).str(" " + NONNULL_PREFIX).str(stem)
                            .str("()")
                            .oB()
                            .str("return ").str(importedName(CODEHELPERS)).str(".nonnull(").str(getterName).eol("());")
                        .cB();
                }
                // a default requireFoo
                case AnydataStatement stmt -> generateRequireMethod(bb, method);
                case AnyxmlStatement stmt -> generateRequireMethod(bb, method);
                case LeafEffectiveStatement stmt -> generateRequireMethod(bb, method);
                case LeafListEffectiveStatement stmt -> generateRequireMethod(bb, method);
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

    private void generateRequireMethod(final BlockBuilder bb, final MethodSignature method) {
        final var getterName = method.name();
        final var stem = getterName.substring(3);

        bb
            .nl()
            .txt(accessorJavadoc(method, ", guaranteed to be non-null", NSEE))
            .str("default ").str(importedNonNull(method.returnType())).str(" " + REQUIRE_PREFIX).str(stem).str("()")
                .oB()
                .str("return ").str(importedName(CODEHELPERS)).str(".require(").str(getterName)
                    // FIXME: property name!
                    .str("(), ").jStr(stem.toLowerCase(Locale.ROOT)).eol(");")
            .cB();
    }

    // FIXME: return a Block
    private String accessorJavadoc(final MethodSignature method, final String orString) {
        return accessorJavadoc(method, orString, null);
    }

    // FIXME: return a Block
    private String accessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var optDescription = method.statement()
            .findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        if (optDescription.isEmpty()) {
            return simpleAccessorJavadoc(method, orString, exception);
        }

        final var reference = optDescription.orElseThrow();
        final var propName = propertyNameFromGetter(method);
        final var bb = template.newBlockBuilder()
            .str("Return ").str(propName).str(orString).eol(".")
            .blk(formatReference(reference))
            .nl()
            .str("@return {@code ").str(importedType(method)).str("} ").str(propName).str(orString).eol(".");
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).eol(" is not present");
        }
        return bb.toJavadocBlock();
    }

    // FIXME: return a Block
    private String simpleAccessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var propName = propertyNameFromGetter(method);

        final var bb = template.newBlockBuilder()
            .str("{@return {@code ").str(importedType(method)).str("} ").str(propName).str(orString).eol("}");
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

    private String nullableType(final Type type) {
        if (isObject(type) && type instanceof ParameterizedType param
            && (Types.isMapType(param) || Types.isListType(param) || Types.isSetType(param))) {
            return template.importedNullable(type);
        }
        return template.importedName(type);
    }

    // The return type has a package, so it's not a primitive type
    private static boolean isObject(final Type type) {
        return !type.packageName().isEmpty();
    }

    private BlockFragment generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.annotations();
        if (annotations.isEmpty()) {
            return bb -> {
                // no-op
            };
        }

        return bb -> {
            for (var annotation : annotations) {
                // FIXME: what is this check doing?
                if (!BaseYangTypes.BOOLEAN_TYPE.equals(method.returnType())
                    || !(annotation instanceof OverrideAnnotation)) {
                    bb.blk(template.generateAnnotation(annotation));
                }
            }
        };
    }


//  private @NonNull BlockBuilder generateMethod(final MethodSignature method) {
//      return newBlockBuilder()
//          .blk(generateJavadoc(method))
//          .blk(generateAnnotations(method))
//          .str(importedReturnType(method)).sp().str(method.name()).eol("();");
//  }
//
//  private static @Nullable BlockBuilder generateJavadoc(final MethodSignature method) {
//      final var optDescription = method.statement()
//          .findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
//      if (optDescription.isEmpty()) {
//          return null;
//      }
//
//      // FIXME: use a BlockBuilder
//      final var sb = new StringBuilder();
//      final var reference = optDescription.orElseThrow();
//      if (reference != null) {
//          sb.append(formatReference(reference).toRawString());
//      }
//      if (sb.isEmpty()) {
//          return null;
//      }
//
//      final var bb = Block.builder();
//      appendAsJavadoc(bb, sb.toString());
//      return bb;
//  }

    private BlockFragment generateAnnotations(final MethodSignature method) {
        final var annotations = method.annotations();
        if (annotations.isEmpty()) {
            return bb -> {
                // no-op
            };
        }

        return bb -> {
            for (var annotation : annotations) {
                bb.blk(template.generateAnnotation(annotation));
            }
        };
    }
}
