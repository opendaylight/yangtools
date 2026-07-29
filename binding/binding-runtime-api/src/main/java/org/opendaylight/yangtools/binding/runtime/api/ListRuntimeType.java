/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.InterfaceArchetype;
import org.opendaylight.yangtools.binding.model.api.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code list} statement.
 */
@NonNullByDefault
public sealed interface ListRuntimeType extends AugmentableRuntimeType, DataRuntimeType {
    /**
     * A {@link ListRuntimeType} for lists that have a {@code key} statement.
     */
    non-sealed interface WithKey extends ListRuntimeType {
        @Override
        EntryObjectArchetype javaType();

        /**
         * {@return the archetype for this list's {@code key} statement}
         */
        KeyArchetype keyType();
    }

    /**
     * A {@link ListRuntimeType} for lists that do not have a {@code key} statement.
     */
    non-sealed interface WithoutKey extends ListRuntimeType {
        @Override
        ItemObjectArchetype javaType();
    }

    @Override
    InterfaceArchetype javaType();

    @Override
    ListEffectiveStatement statement();
}
