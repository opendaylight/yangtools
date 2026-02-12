/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;

/**
 * The {@link Archetype} for {@link DataRoot} specializations.
 * @since 15.0.0
 */
@NonNullByDefault
public record DataRootArchetype() implements Archetype {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    public interface Builder extends GeneratedTypeBuilderBase<Builder> {
        @Override
        DataRootArchetype build();
    }

    public DataRootArchetype {
//        super(builder.getIdentifier());
//        comment = builder.getComment();
//        annotations = toUnmodifiableAnnotations(builder.getAnnotations());
//        implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
//        constants = makeUnmodifiable(builder.getConstants());
//        enumerations = List.copyOf(builder.getEnumerations());
//        methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
//        enclosedTypes = List.copyOf(builder.getEnclosedTransferObjects());
//        properties = toUnmodifiableProperties(builder.getProperties());
//        isAbstract = builder.isAbstract();
//        definition = builder.getYangSourceDefinition().orElse(null);
    }

    @Override
    public TypeComment getComment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public List<Type> getImplements() {
        return List.of();
    }

    @Override
    public List<GeneratedType> getEnclosedTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Enumeration> getEnumerations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Constant> getConstantDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeneratedProperty> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public @NonNull JavaTypeName getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModuleName() {
        // TODO Auto-generated method stub
        return null;
    }
}
