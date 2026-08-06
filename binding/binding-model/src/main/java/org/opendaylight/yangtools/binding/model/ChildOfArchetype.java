/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * An {@link AugmentableArchetype} that implements {@link ChildOf}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ChildOfArchetype extends AugmentableArchetype
        permits ChildOfArchetype.OfList, ContainerObjectArchetype {
    /**
     * A {@link ChildOfArchetype} for a {@code list} statement.
     */
    sealed interface OfList extends ChildOfArchetype permits EntryObjectArchetype, ItemObjectArchetype {
        @Override
        ListEffectiveStatement statement();
    }

    @Override
    @SuppressWarnings("rawtypes")
    Class<? extends ChildOf> contract();

    /**
     * {@return the parent type name}
     */
    TypeName parentName();
}
