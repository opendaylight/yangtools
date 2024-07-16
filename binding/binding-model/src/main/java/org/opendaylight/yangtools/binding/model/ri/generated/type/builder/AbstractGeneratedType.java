/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.api.AbstractType;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
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
    private final Archetype<?> archetype;
    private final TypeComment comment;
    private final List<AnnotationType> annotations;
    private final List<Type> implementsTypes;
    private final List<Enumeration> enumerations;
    private final List<Constant> constants;
    private final List<MethodSignature> methodSignatures;
    private final List<GeneratedType> enclosedTypes;
    private final List<GeneratedProperty> properties;
    private final boolean isAbstract;
    private final YangSourceDefinition definition;

    AbstractGeneratedType(final AbstractGeneratedTypeBuilder<?> builder) {
        archetype = builder.getArchetype();
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

    AbstractGeneratedType(final Archetype<?> archetype, final TypeComment comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
            final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
            final List<GeneratedPropertyBuilder> propertyBuilders) {
        this.archetype = requireNonNull(archetype);
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

    protected static final <T> List<T> makeUnmodifiable(final List<T> list) {
        return switch (list.size()) {
            case 0 -> Collections.emptyList();
            case 1 -> Collections.singletonList(list.getFirst());
            default -> Collections.unmodifiableList(list);
        };
    }

    protected static <T> Set<T> makeUnmodifiable(final Set<T> set) {
        return switch (set.size()) {
            case 0 -> Collections.emptySet();
            case 1 -> Collections.singleton(set.iterator().next());
            default -> Collections.unmodifiableSet(set);
        };
    }

    private static List<GeneratedType> toUnmodifiableEnclosedTypes(
            final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders) {
        final ArrayList<GeneratedType> enclosedTypesList = new ArrayList<>(enclosedGenTypeBuilders.size()
                + enclosedGenTOBuilders.size());
        for (final GeneratedTypeBuilder builder : enclosedGenTypeBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.build());
            }
        }

        for (final GeneratedTOBuilder builder : enclosedGenTOBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.build());
            }
        }

        return makeUnmodifiable(enclosedTypesList);
    }

    protected static final List<AnnotationType> toUnmodifiableAnnotations(
            final List<AnnotationTypeBuilder> annotationBuilders) {
        final List<AnnotationType> annotationList = new ArrayList<>(annotationBuilders.size());
        for (final AnnotationTypeBuilder builder : annotationBuilders) {
            annotationList.add(builder.build());
        }
        return makeUnmodifiable(annotationList);
    }

    protected final List<MethodSignature> toUnmodifiableMethods(final List<MethodSignatureBuilder> methodBuilders) {
        final List<MethodSignature> methods = new ArrayList<>(methodBuilders.size());
        for (final MethodSignatureBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance(this));
        }
        return makeUnmodifiable(methods);
    }

    protected final Set<MethodSignature> toUnmodifiableMethods(final Set<MethodSignatureBuilder> getters) {
        final Set<MethodSignature> methods = new HashSet<>(getters.size());
        for (final MethodSignatureBuilder methodBuilder : getters) {
            methods.add(methodBuilder.toInstance(this));
        }
        return makeUnmodifiable(methods);
    }

    protected final List<Enumeration> toUnmodifiableEnumerations(final List<EnumBuilder> enumBuilders) {
        final List<Enumeration> enums = new ArrayList<>(enumBuilders.size());
        for (final EnumBuilder enumBuilder : enumBuilders) {
            enums.add(enumBuilder.toInstance());
        }
        return makeUnmodifiable(enums);
    }

    protected static final List<GeneratedProperty> toUnmodifiableProperties(
            final List<GeneratedPropertyBuilder> methodBuilders) {
        final var methods = new ArrayList<GeneratedProperty>(methodBuilders.size());
        for (var methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance());
        }
        return makeUnmodifiable(methods);
    }

    @Override
    public final Archetype<?> archetype() {
        return archetype;
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
