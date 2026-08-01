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
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link RpcOutput} specializations.
 *
 * @since 16.0.0
 */
public sealed interface RpcOutputArchetype extends AugmentableArchetype permits RpcOutputArchetypeImpl {
    /**
     * A builder of {@link RpcOutputArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, OutputEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final OutputEffectiveStatement statement,
                final List<GroupingArchetype> groupings) {
            super(typeName, statement, groupings);
        }

        @Override
        public RpcOutputArchetype build() {
            return new RpcOutputArchetypeImpl(typeName, statement, implementsTypes, methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<RpcOutputArchetype> archetypeClass() {
            return RpcOutputArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final OutputEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        return new Builder(typeName, statement, groupings);
    }

    @Override
    OutputEffectiveStatement statement();
}
