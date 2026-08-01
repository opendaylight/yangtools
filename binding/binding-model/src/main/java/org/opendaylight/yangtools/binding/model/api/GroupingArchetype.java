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
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link Grouping} specializations.
 *
 * @since 16.0.0
 */
public sealed interface GroupingArchetype extends DataContainerArchetype permits GroupingArchetypeImpl {
    /**
     * A builder of {@link GroupingArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, GroupingEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final GroupingEffectiveStatement statement,
                final List<GroupingArchetype> groupings) {
            super(typeName, statement, groupings);
        }

        @Override
        public GroupingArchetype build() {
            return new GroupingArchetypeImpl(typeName, statement, implementsTypes, methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<GroupingArchetype> archetypeClass() {
            return GroupingArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final GroupingEffectiveStatement statement,
            final List<GroupingArchetype> groupings) {
        return new Builder(typeName, statement, groupings);
    }

    @Override
    GroupingEffectiveStatement statement();
}
