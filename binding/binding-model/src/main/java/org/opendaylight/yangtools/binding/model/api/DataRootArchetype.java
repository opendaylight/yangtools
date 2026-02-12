/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;

/**
 * The {@link Archetype} for {@link DataRoot} specializations.
 * @since 15.0.0
 */
public record DataRootArchetype(
        @NonNull JavaTypeName getIdentifier,
        @NonNull List<Type> getImplements,
        @NonNull List<MethodSignature> getMethodDefinitions,
        @NonNull List<GeneratedType> getEnclosedTypes,
        @NonNull List<Enumeration> getEnumerations,
        @Nullable String getDescription,
        @Nullable String getReference,
        @Nullable String getModuleName) implements Archetype {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    public interface Builder extends GeneratedTypeBuilderBase<Builder> {
        @Override
        DataRootArchetype build();
    }

    public DataRootArchetype {
        requireNonNull(getIdentifier);
        getImplements = List.copyOf(getImplements);
        getMethodDefinitions = List.copyOf(getMethodDefinitions);
        getEnclosedTypes = List.copyOf(getEnclosedTypes);
        getEnumerations = List.copyOf(getEnumerations);

//        comment = builder.getComment();
//        properties = toUnmodifiableProperties(builder.getProperties());
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
    public List<Constant> getConstantDefinitions() {
        return List.of();
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
}
