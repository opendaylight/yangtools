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
import java.util.Optional;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;

/**
 * The {@link Archetype} for {@link DataRoot} specializations.
 * @since 15.0.0
 */
@Beta
public non-sealed interface DataRootArchetype extends Archetype {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    @Beta
    public interface Builder extends GeneratedTypeBuilderBase<Builder> {
        @Override
        DataRootArchetype build();
    }

    @Override
    default List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

    @Override
    default Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.empty();
    }

    @Override
    default boolean isAbstract() {
        return true;
    }
}
