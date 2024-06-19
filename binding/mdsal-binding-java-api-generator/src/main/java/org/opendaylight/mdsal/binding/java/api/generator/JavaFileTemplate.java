/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;

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
     * {@code java.lang.Override} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName OVERRIDE = JavaTypeName.create(Override.class);

    /**
     * {@code java.lang.SuppressWarnings} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName SUPPRESS_WARNINGS = JavaTypeName.create(SuppressWarnings.class);

    /**
     * {@code java.util.Arrays} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName JU_ARRAYS = JavaTypeName.create(Arrays.class);
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
     * {@code org.eclipse.jdt.annotation.NonNull} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NONNULL = JavaTypeName.create("org.eclipse.jdt.annotation", "NonNull");
    /**
     * {@code org.eclipse.jdt.annotation.Nullable} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName NULLABLE = JavaTypeName.create("org.eclipse.jdt.annotation", "Nullable");

    /**
     * {@code org.opendaylight.yangtools.yang.binding.CodeHelpers} as a JavaTypeName.
     */
    static final @NonNull JavaTypeName CODEHELPERS = JavaTypeName.create(CodeHelpers.class);

    private final AbstractJavaGeneratedType javaType;
    private final GeneratedType type;

    JavaFileTemplate(final @NonNull GeneratedType type) {
        this(new TopLevelJavaGeneratedType(type), type);
    }

    JavaFileTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        this.javaType = requireNonNull(javaType);
        this.type = requireNonNull(type);
    }

    final AbstractJavaGeneratedType javaType() {
        return javaType;
    }

    final GeneratedType type() {
        return type;
    }

    final String generateImportBlock() {
        verify(javaType instanceof TopLevelJavaGeneratedType);
        return ((TopLevelJavaGeneratedType) javaType).imports().map(name -> "import " + name + ";\n")
                .collect(Collectors.joining());
    }

    final @NonNull String importedJavadocName(final @NonNull Type intype) {
        return importedName(intype instanceof ParameterizedType ? ((ParameterizedType) intype).getRawType() : intype);
    }

    final @NonNull String importedName(final @NonNull Type intype) {
        return javaType.getReferenceString(intype);
    }

    final @NonNull String importedName(final @NonNull Type intype, final @NonNull String annotation) {
        return javaType.getReferenceString(intype, annotation);
    }

    final @NonNull String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    final @NonNull String importedName(final @NonNull JavaTypeName intype) {
        return javaType.getReferenceString(intype);
    }

    final @NonNull String importedNonNull(final @NonNull Type intype) {
        return importedName(intype, importedName(NONNULL));
    }

    final @NonNull String importedNullable(final @NonNull Type intype) {
        return importedName(intype, importedName(NULLABLE));
    }

    // Exposed for BuilderTemplate
    boolean isLocalInnerClass(final JavaTypeName name) {
        final Optional<JavaTypeName> optEnc = name.immediatelyEnclosingClass();
        return optEnc.isPresent() && type.getIdentifier().equals(optEnc.get());
    }

    final CharSequence generateInnerClass(final GeneratedType innerClass) {
        if (!(innerClass instanceof GeneratedTransferObject)) {
            return "";
        }

        final GeneratedTransferObject gto = (GeneratedTransferObject) innerClass;
        final NestedJavaGeneratedType innerJavaType = javaType.getEnclosedType(innerClass.getIdentifier());
        return gto.isUnionType() ? new UnionTemplate(innerJavaType, gto).generateAsInnerClass()
                : new ClassTemplate(innerJavaType, gto).generateAsInnerClass();
    }

    /**
     * Return imported name of java.util class, whose hashCode/equals methods we want to invoke on the property. Returns
     * {@link Arrays} if the property is an array, {@link Objects} otherwise.
     *
     * @param property Generated property
     * @return Imported class name
     */
    final String importedUtilClass(final GeneratedProperty property) {
        return importedName(property.getReturnType().getName().indexOf('[') != -1 ? JU_ARRAYS : JU_OBJECTS);
    }

    static final Restrictions restrictionsForSetter(final Type actualType) {
        return actualType instanceof GeneratedType ? null : getRestrictions(actualType);
    }

    static final Restrictions getRestrictions(final Type type) {
        if (type instanceof ConcreteType) {
            return ((ConcreteType) type).getRestrictions();
        }
        if (type instanceof GeneratedTransferObject) {
            return ((GeneratedTransferObject) type).getRestrictions();
        }
        return null;
    }
}
