/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link YangData} specializations.
 *
 * @since 16.0.0
 */
public sealed interface YangDataArchetype extends DataContainerArchetype permits YangDataArchetypeImpl {
    /**
     * A builder of {@link YangDataArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, YangDataEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final YangDataEffectiveStatement statement) {
            super(typeName, statement);
        }

        @Override
        public YangDataArchetype build() {
            return new YangDataArchetypeImpl(typeName, statement, implementsTypes(), methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<YangDataArchetype> archetypeClass() {
            return YangDataArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final YangDataEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    YangDataEffectiveStatement statement();
}
