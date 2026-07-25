/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public abstract sealed class AbstractGeneratedTypeBuilder<
        T extends GeneratedTypeBuilderBase<T>,
        S extends EffectiveStatement<?, ?>> extends AbstractTypeBuilder implements GeneratedTypeBuilderBase<T>
        permits LegacyArchetypeBuilder, DataRootArchetypeBuilder {
    protected final @NonNull S statement;

    private List<AnnotationTypeBuilder> annotationBuilders = List.of();
    private List<Type> implementsTypes = List.of();
    private List<Constant> constants = List.of();
    private List<MethodSignatureBuilder> methodDefinitions = List.of();
    private List<Archetype> enclosedTypes = List.of();

    @NonNullByDefault
    AbstractGeneratedTypeBuilder(final JavaTypeName typeName, final S statement) {
        super(typeName);
        this.statement = requireNonNull(statement);
    }

    @NonNullByDefault
    final List<AnnotationType> getAnnotations() {
        final var size = annotationBuilders.size();
        return switch (size) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(annotationBuilders.getFirst().build());
            case 2 -> List.of(annotationBuilders.getFirst().build(), annotationBuilders.getLast().build());
            default -> {
                final var tmp = new ArrayList<AnnotationType>(size);
                for (var builder : annotationBuilders) {
                    tmp.add(builder.build());
                }
                yield List.copyOf(tmp);
            }
        };
    }

    @NonNullByDefault
    final List<Type> getImplementsTypes() {
        return switch (implementsTypes.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(implementsTypes.getFirst());
            default -> List.copyOf(implementsTypes);
        };
    }

    @NonNullByDefault
    final List<Constant> getConstants() {
        return switch (constants.size()) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(constants.getFirst());
            case 2 -> List.of(constants.getFirst(), constants.getLast());
            default -> List.copyOf(constants);
        };
    }

    @NonNullByDefault
    final List<MethodSignature> getMethodDefinitions() {
        final var size = methodDefinitions.size();
        return switch (size) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(methodDefinitions.getFirst().build());
            case 2 -> List.of(methodDefinitions.getFirst().build(), methodDefinitions.getLast().build());
            default -> {
                final var tmp = new ArrayList<MethodSignature>(size);
                for (var builder : methodDefinitions) {
                    tmp.add(builder.build());
                }
                yield List.copyOf(tmp);
            }
        };
    }

    protected final List<Archetype> getEnclosedTypes() {
        return enclosedTypes;
    }

    protected abstract @NonNull T thisInstance();

    @Override
    public final T addEnclosedType(final Archetype genType) {
        if (enclosedTypes.contains(requireNonNull(genType))) {
            throw new IllegalArgumentException("This generated type already contains equal enclosing transfer object.");
        }
        enclosedTypes = LazyCollections.lazyAdd(enclosedTypes, genType);
        return thisInstance();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final var builder = new AnnotationTypeBuilderImpl(identifier);

        checkArgument(!annotationBuilders.contains(builder), "This generated type already contains equal annotation.");
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    @Override
    public T addImplementsType(final Type genType) {
        checkArgument(!implementsTypes.contains(requireNonNull(genType)),
            "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        checkArgument(type != null, "Returning Type for Constant cannot be null!");
        checkArgument(name != null, "Name of constant cannot be null!");
        checkArgument(!containsConstant(name),
            "This generated type already contains a \"%s\" constant", name);

        final var constant = new Constant(type, name, value);
        constants = LazyCollections.lazyAdd(constants, constant);
        return constant;
    }

    public boolean containsConstant(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        for (var constant : constants) {
            if (name.equals(constant.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        checkArgument(name != null, "Name of method cannot be null!");
        final MethodSignatureBuilder builder = new MethodSignatureBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        builder.setAbstract(true);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
    }

    public Type getParent() {
        return null;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);

        addToStringAttribute(helper, "constants", constants);
        addToStringAttribute(helper, "enclosedTypes", enclosedTypes);
        addToStringAttribute(helper, "methods", methodDefinitions);
        addToStringAttribute(helper, "annotations", annotationBuilders);
        addToStringAttribute(helper, "implements", implementsTypes);

        return helper;
    }
}
