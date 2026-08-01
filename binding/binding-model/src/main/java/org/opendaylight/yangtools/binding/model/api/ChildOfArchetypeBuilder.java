/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common base class for {@link ChildOfArchetype} builder implementations.
 *
 * @param <B> concrete builder type
 * @param <S> EffectiveStatement type
 */
abstract sealed class ChildOfArchetypeBuilder<
        B extends DataContainerArchetypeBuilder<B, S>,
        S extends EffectiveStatement<?, ?>>  extends DataContainerArchetypeBuilder<B, S>
        permits ContainerObjectArchetype.Builder, EntryObjectArchetype.Builder, ItemObjectArchetype.Builder {
    final @NonNull JavaTypeName parentName;

    @NonNullByDefault
    ChildOfArchetypeBuilder(final JavaTypeName typeName, final S statement, final JavaTypeName parentName) {
        super(typeName, statement);
        this.parentName = requireNonNull(parentName);
    }
}
