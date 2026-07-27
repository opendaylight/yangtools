/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link Augmentation} specializations.
 *
 * @since 16.0.0
 */
public sealed interface AugmentationArchetype extends InterfaceArchetype permits AugmentationArchetypeImpl {
    /**
     * A builder of {@link AugmentationArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, AugmentEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final AugmentEffectiveStatement statement) {
            super(typeName, statement);
        }

        @Override
        public AugmentationArchetype build() {
            return new AugmentationArchetypeImpl(typeName, statement, annotations(), implementsTypes(), constants(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        @SuppressWarnings("rawtypes")
        Class<AugmentationArchetype> archetypeClass() {
            return AugmentationArchetype.class;
        }


        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final AugmentEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    AugmentEffectiveStatement statement();
}
