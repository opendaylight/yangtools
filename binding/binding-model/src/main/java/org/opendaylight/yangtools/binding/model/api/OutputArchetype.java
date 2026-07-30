/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link RpcOutput} specializations.
 *
 * @since 16.0.0
 */
public sealed interface OutputArchetype extends AugmentableArchetype permits OutputArchetypeImpl {
    /**
     * A builder of {@link OutputArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, OutputEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final OutputEffectiveStatement statement) {
            super(typeName, statement);
            // FIXME: OutputArchetype should fill this in by itself
            addImplementsType(BindingTypes.RPC_OUTPUT);
        }

        @Override
        public OutputArchetype build() {
            return new OutputArchetypeImpl(typeName, statement, annotations(), implementsTypes(), methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<OutputArchetype> archetypeClass() {
            return OutputArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final OutputEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    OutputEffectiveStatement statement();
}
