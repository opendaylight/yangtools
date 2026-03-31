/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import javax.management.ConstructorParameters;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RestrictedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMember;

/**
 * Base Java file template. Contains a non-null type and imports which the generated code refers to.
 */
class JavaFileTemplate {
    /**
     * {@code java.lang.Class} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CLASS = JavaTypeName.create(Class.class);
    /**
     * {@code java.lang.Deprecated} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName DEPRECATED = JavaTypeName.create(Deprecated.class);
    /**
     * {@code java.lang.IllegalArgumentException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName IAE = JavaTypeName.create(IllegalArgumentException.class);
    /**
     * {@code java.lang.NullPointerException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NPE = JavaTypeName.create(NullPointerException.class);
    /**
     * {@code java.lang.NoSuchElementException} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NSEE = JavaTypeName.create(NoSuchElementException.class);
    /**
     * {@code java.lang.Object} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName OBJECT = JavaTypeName.create(Object.class);
    /**
     * {@code java.lang.Override} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName OVERRIDE = JavaTypeName.create(Override.class);
    /**
     * {@code java.lang.void} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName SUPPRESS_WARNINGS = JavaTypeName.create(SuppressWarnings.class);
    /**
     * {@code java.lang.SuppressWarnings} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName VOID = JavaTypeName.create(void.class);

    /**
     * {@code java.util.Arrays} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_ARRAYS = JavaTypeName.create(Arrays.class);
    /**
     * {@code java.util.HashMap} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_BASE64 = JavaTypeName.create(Base64.class);
    /**
     * {@code java.util.HashMap} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_HASHMAP = JavaTypeName.create(HashMap.class);
    /**
     * {@code java.util.List} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_LIST = JavaTypeName.create(List.class);
    /**
     * {@code java.util.Map} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_MAP = JavaTypeName.create(Map.class);
    /**
     * {@code java.util.Objects} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_OBJECTS = JavaTypeName.create(Objects.class);
    /**
     * {@code java.util.regex.Pattern} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JUR_PATTERN = JavaTypeName.create(Pattern.class);

    /**
     * {@code javax.annotation.processing.Generated} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName GENERATED = JavaTypeName.create(Generated.class);

    /**
     * {@code javax.management.ConstructorParameters} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CONSTRUCTOR_PARAMETERS = JavaTypeName.create(ConstructorParameters.class);

    /**
     * {@code org.eclipse.jdt.annotation.NonNull} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NONNULL = JavaTypeName.create(NonNull.class);
    /**
     * {@code org.eclipse.jdt.annotation.NonNullByDefault} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NONNULL_BY_DEFAULT = JavaTypeName.create(NonNullByDefault.class);
    /**
     * {@code org.eclipse.jdt.annotation.Nullable} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NULLABLE = JavaTypeName.create(Nullable.class);

    /**
     * {@code org.opendaylight.yangtools.binding.lib.CodeHelpers} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CODEHELPERS = JavaTypeName.create(CodeHelpers.class);

    /**
     * {@code com.google.common.base.MoreObjects} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName MOREOBJECTS = JavaTypeName.create(MoreObjects.class);

    private static final CharMatcher AMP_MATCHER = CharMatcher.is('&');
    private static final Pattern TAIL_COMMENT_PATTERN = Pattern.compile("*/", Pattern.LITERAL);

    private final @NonNull AbstractJavaGeneratedType javaType;
    private final @NonNull GeneratedType type;

    JavaFileTemplate(final @NonNull GeneratedType type) {
        this(new TopLevelJavaGeneratedType(type), type);
    }

    @NonNullByDefault
    JavaFileTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        this.javaType = requireNonNull(javaType);
        this.type = requireNonNull(type);
    }

    final @NonNull AbstractJavaGeneratedType javaType() {
        return javaType;
    }

    final @NonNull GeneratedType type() {
        return type;
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
    final String importedReturnType(final TypeMember member) {
        return importedName(member.getReturnType());
    }

    @NonNullByDefault
    final String fullyQualifiedNonNull(final Type intype) {
        return fullyQualifiedName(intype, importedName(NONNULL));
    }

    @NonNullByDefault
    final String fullyQualifiedName(final Type intype, final String annotation) {
        return javaType.getFullyQualifiedReference(intype, annotation);
    }

    // Exposed for BuilderTemplate
    boolean isLocalInnerClass(final JavaTypeName name) {
        final var enclosing = name.immediatelyEnclosingClass();
        return enclosing != null && type.name().equals(enclosing);
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
    final @NonNull String importedUtilClass(final Type returnType) {
        return importedName(returnType.simpleName().indexOf('[') != -1 ? JU_ARRAYS : JU_OBJECTS);
    }

    final @NonNull String generatedAnnotation() {
        return "@" + importedName(GENERATED) + "(\"mdsal-binding-generator\")";
    }

    static final @Nullable Restrictions restrictionsForSetter(final Type actualType) {
        return switch (actualType) {
            case GeneratedType genType -> null;
            case RestrictedType restricted -> restricted.restrictions();
            case null, default -> null;
        };
    }

    /**
     * Generate a call to {@link Object#clone()} if target field represents an array. Returns an empty string otherwise.
     *
     * @param property Generated property
     * @return The string used to clone the property, or an empty string
     */
    static final String cloneCall(final GeneratedProperty property) {
        return property.getReturnType().simpleName().endsWith("[]") ? ".clone()" : "";
    }

    static final @Nullable MethodSignature getterByName(final Collection<MethodSignature> methods,
            final String implMethodName) {
        for (var method : methods) {
            final var methodName = method.getName();
            if (Naming.isGetterMethodName(methodName) && isSameProperty(method.getName(), implMethodName)) {
                return method;
            }
        }
        return null;
    }

    static final @NonNull String propertyNameFromGetter(final MethodSignature getter) {
        return propertyNameFromGetter(getter.getName());
    }

    static final @NonNull String propertyNameFromGetter(final String getterName) {
        final String prefix;
        if (Naming.isGetterMethodName(getterName)) {
            prefix = Naming.GETTER_PREFIX;
        } else if (Naming.isNonnullMethodName(getterName)) {
            prefix = Naming.NONNULL_PREFIX;
        } else if (Naming.isRequireMethodName(getterName)) {
            prefix = Naming.REQUIRE_PREFIX;
        } else {
            throw new IllegalArgumentException(getterName + " is not a getter");
        }
        return Naming.toFirstLower(getterName.substring(prefix.length()));
    }

    /**
     * Check whether specified method has an attached annotation which corresponds to {@code @Override}.
     *
     * @param method Method to examine
     * @return True if there is an override annotation
     */
    static boolean hasOverrideAnnotation(final MethodSignature method) {
        for (var annotation : method.getAnnotations()) {
            if (OVERRIDE.equals(annotation.name())) {
                return true;
            }
        }
        return false;
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
