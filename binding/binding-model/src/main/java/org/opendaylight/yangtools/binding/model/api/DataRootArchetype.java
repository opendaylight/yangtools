/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link DataRoot} specializations.
 *
 * @since 15.0.0
 */
// FIXME: seal to allow only DataRootArchetypeImpl
@Beta
public non-sealed interface DataRootArchetype extends InterfaceArchetype {
    /**
     * A builder of {@link DataRootArchetype} instances.
     */
    @Beta
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, ModuleEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ModuleEffectiveStatement statement) {
            super(typeName, statement);
        }

        @Override
        public DataRootArchetype build() {
            return new DataRootArchetypeImpl(typeName, statement, implementsTypes(), methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<DataRootArchetype> archetypeClass() {
            return DataRootArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final ModuleEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    ModuleEffectiveStatement statement();
}
