/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultActionRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.OperationArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Abstract class for generators dealing with {@link ActionEffectiveStatement}.
 */
abstract sealed class AbstractActionGenerator extends OperationGenerator<ActionEffectiveStatement, ActionRuntimeType>
        permits ActionGenerator, KeyedListActionGenerator {
    @NonNullByDefault
    AbstractActionGenerator(final ActionEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.ACTION;
    }

    @Override
    final ClassPlacement classPlacement() {
        // We do not generate Actions for groupings as they are inexact, and do not capture an actual instantiation --
        // therefore they do not have an InstanceIdentifier. We still need to allocate a package name for the purposes
        // of generating shared classes for input/output
        return getParent() instanceof GroupingGenerator ? ClassPlacement.PHANTOM : ClassPlacement.TOP_LEVEL;
    }

    @Override
    final OperationArchetype.OfAction createTypeImpl(final TypeName typeName,
            final ActionEffectiveStatement statement, final RpcInputArchetype input, final RpcOutputArchetype output) {
        return createTypeImpl(typeName, statement, input, output, getParent().typeName());
    }

    @NonNullByDefault
    abstract OperationArchetype.OfAction createTypeImpl(TypeName typeName, ActionEffectiveStatement statement,
        RpcInputArchetype input, RpcOutputArchetype output, TypeName parentName);

    @Override
    final CompositeRuntimeTypeBuilder<ActionEffectiveStatement, ActionRuntimeType> createBuilder(
            final ActionEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            ActionRuntimeType build(final Archetype type, final ActionEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultActionRuntimeType((OperationArchetype.OfAction) type, statement, childTypes);
            }
        };
    }
}
