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
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultActionRuntimeType;
import org.opendaylight.yangtools.binding.model.api.ActionArchetype;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.InputArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OutputArchetype;
import org.opendaylight.yangtools.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Generator corresponding to a {@code action} statement used outside of a {@code list} with a {@code key}.
 */
final class ActionGenerator extends AbstractActionGenerator<ActionRuntimeType> {
    @NonNullByDefault
    ActionGenerator(final ActionEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    ActionArchetype createTypeImpl(final JavaTypeName typeName,
            final ActionEffectiveStatement statement, final InputArchetype input, final OutputArchetype output) {
        final var builder = ActionArchetype.builder(typeName, statement, input, output, getParent().typeName());
        defaultImplementedInterace(builder);
        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<ActionEffectiveStatement, ActionRuntimeType> createBuilder(
            final ActionEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            ActionRuntimeType build(final Archetype type, final ActionEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultActionRuntimeType((ActionArchetype) type, statement, childTypes);
            }
        };
    }
}
