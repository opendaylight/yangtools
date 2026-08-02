/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link RpcInput} specializations.
 *
 * @since 16.0.0
 */
public sealed interface RpcInputArchetype extends AugmentableArchetype permits RpcInputArchetypeImpl {
    /**
     * A builder of {@link RpcInputArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, InputEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final InputEffectiveStatement statement,
                final List<GroupingArchetype> groupings) {
            super(typeName, statement, groupings);
        }

        @Override
        public RpcInputArchetype build() {
            return new RpcInputArchetypeImpl(typeName, statement, implementsTypes, methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<RpcInputArchetype> archetypeClass() {
            return RpcInputArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final InputEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        return new Builder(typeName, statement, groupings);
    }

    @NonNullByDefault
    static RpcInputArchetype of(final JavaTypeName typeName, final InputEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<MethodSignature> methods,
            final List<Archetype> enclosedTypes) {
        return new RpcInputArchetypeImpl(typeName, statement, groupings, methods, enclosedTypes);
    }

    @Override
    InputEffectiveStatement statement();
}
