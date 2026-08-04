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
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_ARRAYS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NULLABLE;
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isNonnullMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isRequireMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstLower;

import com.google.common.base.CharMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.OverrideAnnotation;
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
    final String importedReturnType(final MethodSignature method) {
        return importedName(method.returnType());
    }

    @NonNullByDefault
    final String importedReturnType(final GeneratedProperty property) {
        return importedName(property.getReturnType());
    }

    @NonNullByDefault
    final String fullyQualifiedNonNull(final Type intype) {
        return fullyQualifiedName(intype, importedName(NONNULL));
    }

    @NonNullByDefault
    final String fullyQualifiedName(final Type intype, final String annotation) {
        return javaType.getFullyQualifiedReference(intype, annotation);
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke on the property. Returns
     * {@link Arrays} if the property is an array, {@link Objects} otherwise.
     *
     * @param property Generated property
     * @return Imported class name
     */
    final String importedUtilClass(final GeneratedProperty property) {
        return importedUtilClass(property.getReturnType());
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke for a type. Returns
     * {@link Arrays} if the type is an array, {@link Objects} otherwise.
     *
     * @param returnType A property return Type
     * @return Imported class name
     */
    @NonNullByDefault
    final String importedUtilClass(final Type returnType) {
        return importedName(returnType.isArray() ? JU_ARRAYS : JU_OBJECTS);
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

    /**
     * {@return the {@link BlockFragment} used to clone the property, or {@code null}}
     * @param type the type
     */
    static final @Nullable BlockFragment cloneOrNull(final @NonNull Type type) {
        return type.isArray() ? bb -> bb.str(".clone()") : null;
    }

    @NonNullByDefault
    static final boolean isArrayProperty(final GeneratedProperty property) {
        return property.getReturnType().isArray();
    }

    static final @Nullable MethodSignature getterByName(final @NonNull Collection<@NonNull MethodSignature> methods,
            final @NonNull String implMethodName) {
        for (var method : methods) {
            final var methodName = method.name();
            if (isGetterMethodName(methodName) && isSameProperty(method.name(), implMethodName)) {
                return method;
            }
        }
        return null;
    }

    static final @NonNull String propertyNameFromGetter(final MethodSignature getter) {
        return propertyNameFromGetter(getter.name());
    }

    static final @NonNull String propertyNameFromGetter(final String getterName) {
        final String prefix;
        if (isGetterMethodName(getterName)) {
            prefix = GETTER_PREFIX;
        } else if (isNonnullMethodName(getterName)) {
            prefix = NONNULL_PREFIX;
        } else if (isRequireMethodName(getterName)) {
            prefix = REQUIRE_PREFIX;
        } else {
            throw new IllegalArgumentException(getterName + " is not a getter");
        }
        return toFirstLower(getterName.substring(prefix.length()));
    }

    /**
     * Check whether specified method has an attached annotation which corresponds to {@code @Override}.
     *
     * @param method Method to examine
     * @return True if there is an override annotation
     */
    static boolean hasOverrideAnnotation(final MethodSignature method) {
        return method.annotations().stream().anyMatch(OverrideAnnotation.class::isInstance);
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

    private static boolean isSameProperty(final String getterName1, final String getterName2) {
        return propertyNameFromGetter(getterName1).equals(propertyNameFromGetter(getterName2));
    }
}
