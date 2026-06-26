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
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.DataRootArchetypeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * The {@link Archetype} for {@link DataRoot} specializations.
 *
 * @since 15.0.0
 */
@Beta
public non-sealed interface DataRootArchetype
        extends Archetype.WithStatement<ModuleEffectiveStatement>, LegacyArchetype<ModuleEffectiveStatement> {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    @Beta
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder> permits DataRootArchetypeBuilder {
        @Override
        @Deprecated(forRemoval = true)
        Builder addComment(TypeComment comment);

        @Override
        DataRootArchetype build();
    }

    @Override
    default TypeComment getComment() {
        return DocUtils.typeCommentOf(statement().toDataNodeContainer());
    }

    @Override
    default String getDescription() {
        final var stmt = statement().descriptionStatement();
        return stmt == null ? null : stmt.argument();
    }

    @Override
    default String getReference() {
        final var stmt = statement().referenceStatement();
        return stmt == null ? null : stmt.argument();
    }

    @Override
    default String getModuleName() {
        return statement().argument().getLocalName();
    }

    @Override
    default YangSourceDefinition yangSourceDefinition() {
        return YangSourceDefinition.of(statement());
    }

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
}
