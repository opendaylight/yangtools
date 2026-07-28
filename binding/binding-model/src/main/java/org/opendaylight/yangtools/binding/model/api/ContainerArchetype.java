/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link ChildOf} specializations generated for {@code container} statements. It has
 * two further specializations:
 * <ul>
 *   <li>{@link ContainerArchetype.Presence} for containers with a {@code presence} statement</li>
 *   <li>{@link ContainerArchetype.Structural} for containers without a {@code presence} statement</li>
 * </ul>
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ContainerArchetype extends InterfaceArchetype {
    /**
     * A builder of {@link ContainerArchetype}s.
     */
    final class Builder extends InterfaceArchetypeBuilder<Builder, ContainerEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ContainerEffectiveStatement statement) {
            super(typeName, statement);
        }

        @Override
        public ContainerArchetype build() {
            return statement.presenceStatement() != null
                ? new PresenceContainerArchetype(typeName, statement, annotations(), implementsTypes(), constants(),
                    methodDefinitions(), enclosedTypes())
                : new StructuralContainerArchetype(typeName, statement, annotations(), implementsTypes(), constants(),
                    methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<ContainerArchetype> archetypeClass() {
            return ContainerArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    /**
     * A {@link ContainerArchetype} for presence containers.
     */
    sealed interface Presence extends ContainerArchetype permits PresenceContainerArchetype {
        /**
         * {@return the {@code presence} statement}
         */
        default PresenceEffectiveStatement presence() {
            return statement().getPresenceStatement();
        }
    }

    /**
     * A {@link ContainerArchetype} for structural containers.
     */
    sealed interface Structural extends ContainerArchetype permits StructuralContainerArchetype {
        // nothing else
    }

    static Builder builder(final JavaTypeName typeName, final ContainerEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }

    @Override
    ContainerEffectiveStatement statement();
}
