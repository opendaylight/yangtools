/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultRpcRuntimeType;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.RpcArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * Generator corresponding to a {@code rpc} statement.
 */
final class RpcGenerator extends OperationGenerator<RpcEffectiveStatement, RpcRuntimeType> {
    @NonNullByDefault
    RpcGenerator(final RpcEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.RPC;
    }

    @Override
    ClassPlacement classPlacement() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    RpcArchetype createTypeImpl(final TypeName typeName, final RpcEffectiveStatement statement,
            final RpcInputArchetype input, final RpcOutputArchetype output) {
        return RpcArchetype.of(typeName, statement, input, output);
    }

    @Override
    CompositeRuntimeTypeBuilder<RpcEffectiveStatement, RpcRuntimeType> createBuilder(
            final RpcEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            RpcRuntimeType build(final Archetype generatedType, final RpcEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultRpcRuntimeType((RpcArchetype) generatedType, statement, childTypes);
            }
        };
    }
}
