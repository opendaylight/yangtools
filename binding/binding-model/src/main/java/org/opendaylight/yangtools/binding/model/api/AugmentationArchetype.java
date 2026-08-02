/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link Augmentation} specializations.
 *
 * @since 16.0.0
 */
public sealed interface AugmentationArchetype extends DataContainerArchetype permits AugmentationArchetypeImpl {
    /**
     * A builder of {@link AugmentationArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, AugmentEffectiveStatement> {
        private final AugmentableArchetype target;

        private Builder(final JavaTypeName typeName, final AugmentEffectiveStatement statement,
                final AugmentableArchetype target) {
            super(typeName, statement);
            this.target = requireNonNull(target);
        }

        @Override
        public AugmentationArchetype build() {
            return new AugmentationArchetypeImpl(typeName, statement, target, implementsTypes(), methodDefinitions(),
                enclosedTypes());
        }

        @Override
        Class<AugmentationArchetype> archetypeClass() {
            return AugmentationArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final AugmentEffectiveStatement statement,
            final AugmentableArchetype target) {
        return new Builder(typeName, statement, target);
    }

    @Override
    AugmentEffectiveStatement statement();

    /**
     * {@return the augmentation target archetype}
     */
    AugmentableArchetype target();
}
