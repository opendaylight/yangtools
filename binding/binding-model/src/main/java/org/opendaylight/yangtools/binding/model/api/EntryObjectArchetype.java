/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link EntryObject} specializations.
 *
 * @since 16.0.0
 */
public sealed interface EntryObjectArchetype extends InterfaceArchetype permits EntryObjectArchetypeImpl {
    /**
     * A builder of {@link EntryObjectArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, ListEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ListEffectiveStatement statement) {
            super(typeName, statement);
        }

        @Override
        public EntryObjectArchetype build() {
            return new EntryObjectArchetypeImpl(typeName, statement, annotations(), implementsTypes(), constants(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<EntryObjectArchetype> archetypeClass() {
            return EntryObjectArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final ListEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    ListEffectiveStatement statement();
}
