/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link RpcInput} specializations.
 *
 * @since 16.0.0
 */
public sealed interface InputArchetype extends InterfaceArchetype permits InputArchetypeImpl {
    /**
     * A builder of {@link InputArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, InputEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final InputEffectiveStatement statement) {
            super(typeName, statement);
            // FIXME: InputArchetype should fill this in by itself
            addImplementsType(BindingTypes.RPC_INPUT);
        }

        @Override
        public InputArchetype build() {
            return new InputArchetypeImpl(typeName, statement, annotations(), implementsTypes(), constants(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<InputArchetype> archetypeClass() {
            return InputArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final InputEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    InputEffectiveStatement statement();
}
