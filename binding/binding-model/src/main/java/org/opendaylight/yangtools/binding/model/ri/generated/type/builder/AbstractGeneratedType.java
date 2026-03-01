/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;

abstract class AbstractGeneratedType implements GeneratedType {
    private final @NonNull JavaTypeName name;
    private final @NonNull List<AnnotationType> annotations;
    private final @NonNull List<Type> implementsTypes;
    private final @NonNull List<EnumTypeObjectArchetype> enumerations;
    private final @NonNull List<Constant> constants;
    private final @NonNull List<MethodSignature> methodSignatures;
    private final @NonNull List<GeneratedType> enclosedTypes;
    private final @NonNull List<GeneratedProperty> properties;
    private final @Nullable YangSourceDefinition definition;
    private final @Nullable TypeComment comment;
    private final boolean isAbstract;

    AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
        name = builder.typeName();
        comment = builder.getComment();
        annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
        constants = makeUnmodifiable(builder.getConstants());
        enumerations = List.copyOf(builder.getEnumerations());
        methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        enclosedTypes = List.copyOf(builder.getEnclosedTransferObjects());
        properties = toUnmodifiableProperties(builder.getProperties());
        isAbstract = builder.isAbstract();
        definition = builder.getYangSourceDefinition().orElse(null);
    }

    AbstractGeneratedType(final @NonNull JavaTypeName typeName, final TypeComment comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders,
            final List<EnumTypeObjectArchetype.Builder> enumBuilders, final List<Constant> constants,
            final List<MethodSignatureBuilder> methodBuilders, final List<GeneratedPropertyBuilder> propertyBuilders) {
        name = requireNonNull(typeName);
        this.comment = comment;
        annotations = toUnmodifiableAnnotations(annotationBuilders);
        this.implementsTypes = makeUnmodifiable(implementsTypes);
        this.constants = makeUnmodifiable(constants);
        enumerations = toUnmodifiableEnumerations(enumBuilders);
        methodSignatures = toUnmodifiableMethods(methodBuilders);
        enclosedTypes = toUnmodifiableEnclosedTypes(enclosedGenTypeBuilders, enclosedGenTOBuilders);
        properties = toUnmodifiableProperties(propertyBuilders);
        this.isAbstract = isAbstract;
        definition = null;
    }

    protected static final <T> @NonNull List<T> makeUnmodifiable(final List<T> list) {
        return switch (list.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(list.getFirst());
            case 2 -> List.copyOf(list);
            default -> Collections.unmodifiableList(list);
        };
    }

    protected static <T> @NonNull Set<T> makeUnmodifiable(final Set<T> set) {
        return switch (set.size()) {
            case 0 -> Set.of();
            case 1 -> Collections.singleton(set.iterator().next());
            case 2 -> Set.copyOf(set);
            default -> Collections.unmodifiableSet(set);
        };
    }

    private static List<GeneratedType> toUnmodifiableEnclosedTypes(
            final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders) {
        final var enclosedTypesList = new ArrayList<GeneratedType>(
            enclosedGenTypeBuilders.size() + enclosedGenTOBuilders.size());
        for (var builder : enclosedGenTypeBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.build());
            }
        }

        for (var builder : enclosedGenTOBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.build());
            }
        }

        return makeUnmodifiable(enclosedTypesList);
    }

    protected static final @NonNull List<AnnotationType> toUnmodifiableAnnotations(
            final List<AnnotationTypeBuilder> annotationBuilders) {
        return makeUnmodifiable(annotationBuilders.stream()
            .map(AnnotationTypeBuilder::build)
            .collect(Collectors.toUnmodifiableList()));
    }

    protected static final List<MethodSignature> toUnmodifiableMethods(
            final List<MethodSignatureBuilder> methodBuilders) {
        final var methods = new ArrayList<MethodSignature>(methodBuilders.size());
        for (var methodBuilder : methodBuilders) {
            methods.add(methodBuilder.build());
        }
        return makeUnmodifiable(methods);
    }

    protected static final Set<MethodSignature> toUnmodifiableMethods(final Set<MethodSignatureBuilder> getters) {
        final var methods = HashSet.<MethodSignature>newHashSet(getters.size());
        for (var methodBuilder : getters) {
            methods.add(methodBuilder.build());
        }
        return makeUnmodifiable(methods);
    }

    protected static final @NonNull List<EnumTypeObjectArchetype> toUnmodifiableEnumerations(
            final List<EnumTypeObjectArchetype.Builder> enumBuilders) {
        return makeUnmodifiable(enumBuilders.stream()
            .map(EnumTypeObjectArchetype.Builder::build)
            .collect(Collectors.toUnmodifiableList()));
    }

    protected static final @NonNull List<GeneratedProperty> toUnmodifiableProperties(
            final List<GeneratedPropertyBuilder> methodBuilders) {
        return makeUnmodifiable(methodBuilders.stream()
            .map(GeneratedPropertyBuilder::toInstance)
            .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public final JavaTypeName name() {
        return name;
    }

    @Override
    public final TypeComment getComment() {
        return comment;
    }

    @Override
    public final List<AnnotationType> getAnnotations() {
        return annotations;
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public final List<Type> getImplements() {
        return implementsTypes;
    }

    @Override
    public final List<GeneratedType> getEnclosedTypes() {
        return enclosedTypes;
    }

    @Override
    public final List<EnumTypeObjectArchetype> getEnumerations() {
        return enumerations;
    }

    @Override
    public final List<Constant> getConstantDefinitions() {
        return constants;
    }

    @Override
    public final List<MethodSignature> getMethodDefinitions() {
        return methodSignatures;
    }

    @Override
    public final List<GeneratedProperty> getProperties() {
        return properties;
    }

    @Override
    public final Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.ofNullable(definition);
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    @NonNullByDefault
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("name", name);

        final var local = comment;
        if (local != null) {
            helper.add("comment", local);
        }
        addToStringAttribute(helper, "annotations", annotations);
        addToStringAttribute(helper, "implements", implementsTypes);
        addToStringAttribute(helper, "enclosedTypes", enclosedTypes);
        addToStringAttribute(helper, "enumerations", enumerations);
        addToStringAttribute(helper, "constants", constants);
        addToStringAttribute(helper, "methods", methodSignatures);
        addToStringAttribute(helper, "properties", properties);

        return helper;
    }

    @NonNullByDefault
    protected static final void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
