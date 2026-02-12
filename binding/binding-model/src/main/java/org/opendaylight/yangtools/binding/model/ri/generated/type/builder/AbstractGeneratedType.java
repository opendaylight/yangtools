/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AbstractType;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;

abstract class AbstractGeneratedType extends AbstractType implements GeneratedType {
    private final @Nullable TypeComment comment;
    private final @NonNull List<AnnotationType> annotations;
    private final @NonNull List<Type> implementsTypes;
    private final @NonNull List<Enumeration> enumerations;
    private final @NonNull List<Constant> constants;
    private final @NonNull List<MethodSignature> methodSignatures;
    private final @NonNull List<GeneratedType> enclosedTypes;
    private final @NonNull List<GeneratedProperty> properties;
    private final boolean isAbstract;
    private final YangSourceDefinition definition;

    AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
        super(builder.getIdentifier());
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

    AbstractGeneratedType(final JavaTypeName identifier, final TypeComment comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
            final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
            final List<GeneratedPropertyBuilder> propertyBuilders) {
        super(identifier);
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

    protected final @NonNull List<MethodSignature> toUnmodifiableMethods(
            final List<MethodSignatureBuilder> methodBuilders) {
        return makeUnmodifiable(methodBuilders.stream()
            .map(builder -> builder.toInstance(this))
            .collect(Collectors.toUnmodifiableList()));
    }

    protected final @NonNull Set<MethodSignature> toUnmodifiableMethods(final Set<MethodSignatureBuilder> getters) {
        return makeUnmodifiable(getters.stream()
            .map(builder -> builder.toInstance(this))
            .collect(Collectors.toUnmodifiableSet()));
    }

    protected static final @NonNull List<Enumeration> toUnmodifiableEnumerations(final List<EnumBuilder> enumBuilders) {
        return makeUnmodifiable(enumBuilders.stream()
            .map(EnumBuilder::toInstance)
            .collect(Collectors.toUnmodifiableList()));
    }

    protected static final @NonNull List<GeneratedProperty> toUnmodifiableProperties(
            final List<GeneratedPropertyBuilder> methodBuilders) {
        return makeUnmodifiable(methodBuilders.stream()
            .map(GeneratedPropertyBuilder::toInstance)
            .collect(Collectors.toUnmodifiableList()));
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
    public final List<Enumeration> getEnumerations() {
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper)
            .omitNullValues()
            .add("comment", comment)
            .add("annotations", annotations)
            .add("enclosedTypes", enclosedTypes)
            .add("enumerations", enumerations)
            .add("constants", constants)
            .add("methodSignatures", methodSignatures);
    }
}
