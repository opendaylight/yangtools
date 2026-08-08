/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.RpcArchetypeImpl;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * An {@link Archetype} for a {@link Rpc} generated for an {@link RpcEffectiveStatement}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface RpcArchetype extends OperationArchetype permits RpcArchetypeImpl {
    /**
     * {@return an RpcArchetype}
     * @param name the archetype's {@link TypeName}}
     * @param statement the {@link RpcEffectiveStatement}
     * @param input the {@link RpcInputArchetype} of the RPC's input
     * @param output the {@link RpcOutputArchetype} of the RPC's output
     */
    static RpcArchetype of(final TypeName name, final RpcEffectiveStatement statement,
            final RpcInputArchetype input, final RpcOutputArchetype output) {
        return new RpcArchetypeImpl(name, statement, input, output);
    }

    @Override
    @SuppressWarnings("rawtypes")
    default Class<Rpc> contract() {
        return Rpc.class;
    }

    @Override
    RpcEffectiveStatement statement();
}
