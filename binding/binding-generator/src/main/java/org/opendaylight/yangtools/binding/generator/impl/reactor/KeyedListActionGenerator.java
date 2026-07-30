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
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultKeyedListActionRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.InputArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;
import org.opendaylight.yangtools.binding.model.api.OutputArchetype;
import org.opendaylight.yangtools.binding.runtime.api.KeyedListActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * Generator corresponding to a {@code action} statement used inside of a {@code list} with a {@code key}.
 */
final class KeyedListActionGenerator extends AbstractActionGenerator<KeyedListActionRuntimeType> {
    @NonNullByDefault
    KeyedListActionGenerator(final ActionEffectiveStatement statement, final EntryObjectGenerator parent) {
        super(statement, parent);
    }

    @Override
    KeyedListActionArchetype createTypeImpl(final JavaTypeName typeName,
            final ActionEffectiveStatement statement, final InputArchetype input, final OutputArchetype output,
            final JavaTypeName parentName) {
        return KeyedListActionArchetype.of(typeName, statement, input, output, parentName);
    }

    @Override
    CompositeRuntimeTypeBuilder<ActionEffectiveStatement, KeyedListActionRuntimeType> createBuilder(
            final ActionEffectiveStatement statement) {
        return new InvokableRuntimeTypeBuilder<>(statement) {
            @Override
            KeyedListActionRuntimeType build(final Archetype type, final ActionEffectiveStatement statement,
                    final List<RuntimeType> childTypes) {
                return new DefaultKeyedListActionRuntimeType((KeyedListActionArchetype) type, statement, childTypes);
            }
        };
    }
}
