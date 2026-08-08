/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * The {@link OperationArchetype} for {@link Action} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ActionArchetype extends OperationArchetype.OfAction permits ActionArchetypeImpl {
    /**
     * {@return an ActionArchetype}
     * @param name the archetype's {@link TypeName}}
     * @param statement the {@link ActionEffectiveStatement}
     * @param input the {@link RpcInputArchetype} of the action's input
     * @param output the {@link RpcOutputArchetype} of the action's output
     * @param parentName the name of the parent archetype
     */
    static ActionArchetype of(final TypeName name, final ActionEffectiveStatement statement,
            final RpcInputArchetype input, final RpcOutputArchetype output, final TypeName parentName) {
        return new ActionArchetypeImpl(name, statement, input, output, parentName);
    }

    @Override
    @SuppressWarnings("rawtypes")
    default Class<Action> contract() {
        return Action.class;
    }
}
