/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.InputArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OutputArchetype;
import org.opendaylight.yangtools.binding.runtime.api.InvokableRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Abstract class for generators dealing with {@link ActionEffectiveStatement}.
 */
abstract sealed class AbstractActionGenerator<R extends InvokableRuntimeType>
        extends OperationGenerator<ActionEffectiveStatement, R>
        permits ActionGenerator, KeyedListActionGenerator {
    @NonNullByDefault
    AbstractActionGenerator(final ActionEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
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
    final Archetype createTypeImpl(final InputArchetype input, final OutputArchetype output) {
        return createTypeImpl(typeName(), statement(), input, output);
    }

    @NonNullByDefault
    abstract Archetype createTypeImpl(JavaTypeName typeName, ActionEffectiveStatement statement, InputArchetype input,
        OutputArchetype output);
}
