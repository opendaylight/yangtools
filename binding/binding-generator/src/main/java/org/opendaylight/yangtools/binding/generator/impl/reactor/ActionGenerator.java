/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.ActionArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Generator corresponding to a {@code action} statement used outside of a {@code list} with a {@code key}.
 */
final class ActionGenerator extends AbstractActionGenerator {
    @NonNullByDefault
    ActionGenerator(final ActionEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    ActionArchetype createTypeImpl(final TypeName typeName,
            final ActionEffectiveStatement statement, final RpcInputArchetype input, final RpcOutputArchetype output,
            final TypeName parentName) {
        return ActionArchetype.of(typeName, statement, input, output, parentName);
    }
}
