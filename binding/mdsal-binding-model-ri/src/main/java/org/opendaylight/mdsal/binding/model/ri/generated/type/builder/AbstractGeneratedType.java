/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.AbstractType;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;

abstract class AbstractGeneratedType extends AbstractType implements GeneratedType {
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
        super(builder.getIdentifier());
        this.comment = builder.getComment();
        this.annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        this.implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
        this.constants = makeUnmodifiable(builder.getConstants());
        this.enumerations = List.copyOf(builder.getEnumerations());
        this.methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        this.enclosedTypes = List.copyOf(builder.getEnclosedTransferObjects());
        this.properties = toUnmodifiableProperties(builder.getProperties());
        this.isAbstract = builder.isAbstract();
        this.definition = builder.getYangSourceDefinition().orElse(null);
    }

    AbstractGeneratedType(final JavaTypeName identifier, final TypeComment comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
            final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
            final List<GeneratedPropertyBuilder> propertyBuilders) {
        super(identifier);
        this.comment = comment;
        this.annotations = toUnmodifiableAnnotations(annotationBuilders);
        this.implementsTypes = makeUnmodifiable(implementsTypes);
        this.constants = makeUnmodifiable(constants);
        this.enumerations = toUnmodifiableEnumerations(enumBuilders);
        this.methodSignatures = toUnmodifiableMethods(methodBuilders);
        this.enclosedTypes = toUnmodifiableEnclosedTypes(enclosedGenTypeBuilders, enclosedGenTOBuilders);
        this.properties = toUnmodifiableProperties(propertyBuilders);
        this.isAbstract = isAbstract;
        this.definition = null;
    }

    protected static final <T> List<T> makeUnmodifiable(final List<T> list) {
        switch (list.size()) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(list.get(0));
            default:
                return Collections.unmodifiableList(list);
        }
    }

    protected static <T> Set<T> makeUnmodifiable(final Set<T> set) {
        switch (set.size()) {
            case 0:
                return Collections.emptySet();
            case 1:
                return Collections.singleton(set.iterator().next());
            default:
                return Collections.unmodifiableSet(set);
        }
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

    protected final List<GeneratedProperty> toUnmodifiableProperties(
            final List<GeneratedPropertyBuilder> methodBuilders) {
        final List<GeneratedProperty> methods = new ArrayList<>(methodBuilders.size());
        for (final GeneratedPropertyBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance());
        }
        return makeUnmodifiable(methods);
    }

    @Override
    public final TypeComment getComment() {
        return this.comment;
    }

    @Override
    public final List<AnnotationType> getAnnotations() {
        return this.annotations;
    }

    @Override
    public final boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public final List<Type> getImplements() {
        return this.implementsTypes;
    }

    @Override
    public final List<GeneratedType> getEnclosedTypes() {
        return this.enclosedTypes;
    }

    @Override
    public final List<Enumeration> getEnumerations() {
        return this.enumerations;
    }

    @Override
    public final List<Constant> getConstantDefinitions() {
        return this.constants;
    }

    @Override
    public final List<MethodSignature> getMethodDefinitions() {
        return this.methodSignatures;
    }

    @Override
    public final List<GeneratedProperty> getProperties() {
        return this.properties;
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
