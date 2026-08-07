/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.GENERATED;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NULLABLE;

import com.google.common.base.CharMatcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Base Java file template. Contains a non-null type and imports which the generated code refers to.
 */
abstract sealed class JavaFileTemplate extends Template permits BaseTemplate {

    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);

    private final @NonNull GeneratedClass javaType;

    @NonNullByDefault
    JavaFileTemplate(final GeneratedClass javaType) {
        this.javaType = requireNonNull(javaType);
    }

    final @NonNull GeneratedClass javaType() {
        return javaType;
    }

    @Override
    final JavaTypeName typeName() {
        return javaType.name();
    }

    final @NonNull BlockBuilder newBlockBuilder() {
        return javaType.newBlockBuilder();
    }

    @NonNullByDefault
    final String importedJavadocName(final Type intype) {
        return importedName(intype instanceof ParameterizedType parameterized ? parameterized.getRawType() : intype);
    }

    @NonNullByDefault
    final String importedName(final Type intype) {
        return javaType.getReferenceString(intype);
    }

    @NonNullByDefault
    final String importedName(final Type intype, final String annotation) {
        return javaType.getReferenceString(intype, annotation);
    }

    @NonNullByDefault
    final String importedName(final JavaTypeName intype) {
        return javaType.getReferenceString(intype);
    }

    @NonNullByDefault
    final String importedNonNull(final Type intype) {
        return importedName(intype, importedName(NONNULL));
    }

    @NonNullByDefault
    final String importedNullable(final Type intype) {
        return importedName(intype, importedName(NULLABLE));
    }

    @NonNullByDefault
    final String fullyQualifiedNonNull(final Type intype) {
        return fullyQualifiedName(intype, importedName(NONNULL));
    }

    @NonNullByDefault
    final String fullyQualifiedName(final Type intype, final String annotation) {
        return javaType.getFullyQualifiedReference(intype, annotation);
    }

    final @NonNull String generatedAnnotation() {
        return "@" + importedName(GENERATED) + "(\"mdsal-binding-generator\")";
    }

    /**
     * Extract a {@link Type}'s {@link Restrictions}.
     *
     * @param actualType the type
     * @return non-{@link Restrictions#isEmpty()} {@link Restrictions} or {@code null}
     */
    static final @Nullable Restrictions restrictionsForSetter(final @NonNull Type actualType) {
        return switch (actualType) {
            case RestrictedType restricted -> {
                final var restrictions = restricted.restrictions();
                yield restrictions.isEmpty() ? null : restrictions;
            }
            default -> null;
        };
    }

    static String encodeJavadocSymbols(final String description) {
        // FIXME: Use String.isBlank()?
        return description == null || description.isEmpty() ? description
            : TAIL_COMMENT_PATTERN.matcher(AMP_MATCHER.replaceFrom(description, "&amp;")).replaceAll("&#42;&#47;");
    }

//    private static void appendPath(final StringBuilder sb, final ModuleEffectiveStatement module,
//            final List<QName> path) {
//        if (!path.isEmpty()) {
//            // FIXME: this is module name, while when we switch, we end up using QName.toString() -- which is weird
//            sb.append(module.argument().getLocalName());
//            XMLNamespace currentNamespace = path.getFirst().getNamespace();
//
//            for (QName pathElement : path) {
//                final XMLNamespace elementNamespace = pathElement.getNamespace();
//                if (!elementNamespace.equals(currentNamespace)) {
//                    sb.append(pathElement);
//                    currentNamespace = elementNamespace;
//                } else {
//                    sb.append(pathElement.getLocalName());
//                }
//            }
//        }
//    }
}
