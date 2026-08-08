/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.KeyedListActionArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Generator corresponding to a {@code action} statement used inside of a {@code list} with a {@code key}.
 */
final class KeyedListActionGenerator extends AbstractActionGenerator {
    @NonNullByDefault
    KeyedListActionGenerator(final ActionEffectiveStatement statement, final EntryObjectGenerator parent) {
        super(statement, parent);
    }

    @Override
    KeyedListActionArchetype createTypeImpl(final TypeName typeName,
            final ActionEffectiveStatement statement, final RpcInputArchetype input, final RpcOutputArchetype output,
            final TypeName parentName) {
        return KeyedListActionArchetype.of(typeName, statement, input, output, parentName);
    }
}
