/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultInputRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * Generator corresponding to an {@code input} statement.
 */
class InputGenerator extends OperationContainerGenerator<InputEffectiveStatement, InputRuntimeType> {
    InputGenerator(final InputEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent, BindingTypes.RPC_INPUT);
    }

    @Override
    final CompositeRuntimeTypeBuilder<InputEffectiveStatement, InputRuntimeType> createBuilder(
            final InputEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            InputRuntimeType build(final GeneratedType type, final InputEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultInputRuntimeType(type, statement, children, augments);
            }
        };
    }
}
