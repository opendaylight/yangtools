/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link DataObject}s specializations generated for {@code list} statements
 * without a {@code key}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ItemObjectArchetype extends ChildOfArchetype.OfList permits ItemObjectArchetypeImpl {
    /**
     * A builder of {@link ItemObjectArchetype}s.
     */
    final class Builder extends ChildOfArchetypeBuilder<Builder, ListEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ListEffectiveStatement statement,
                final JavaTypeName parentName, final List<GroupingArchetype> groupings) {
            super(typeName, statement, groupings, parentName);
        }

        @Override
        public ItemObjectArchetype build() {
            return new ItemObjectArchetypeImpl(typeName, statement, parentName, implementsTypes, methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<ItemObjectArchetype> archetypeClass() {
            return ItemObjectArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    static Builder builder(final JavaTypeName typeName, final ListEffectiveStatement statement,
            final JavaTypeName parentName, final List<GroupingArchetype> groupings) {
        return new Builder(typeName, statement, parentName, groupings);
    }
}
