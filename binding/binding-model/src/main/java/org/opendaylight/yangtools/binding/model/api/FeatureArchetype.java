/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.FeatureArchetypeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * An {@link Archetype} for generated {@link YangFeature}s.
 *
 * @since 16.0.0
 */
@Beta
public non-sealed interface FeatureArchetype extends Archetype.WithStatement<FeatureEffectiveStatement> {
    /**
     * A builder of {@link FeatureArchetype} instances.
     */
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder> permits FeatureArchetypeBuilder {
        @Override
        FeatureArchetype build();
    }

    /**
     * {@return the name of the {@link DataRoot} generated for the module containing the {@code feature} statement}
     */
    @NonNull JavaTypeName dataRoot();

    @Override
    @Deprecated(forRemoval = true)
    default List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default boolean isAbstract() {
        return false;
    }
}
