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
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link ChildOf} specializations generated for {@code container} statements.
 *
 * @since 16.0.0
 */
public sealed interface ContainerObjectArchetype extends ChildOfArchetype permits ContainerObjectArchetypeImpl {
    /**
     * A builder of {@link ContainerObjectArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends ChildOfArchetypeBuilder<Builder, ContainerEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ContainerEffectiveStatement statement,
                final JavaTypeName parentName) {
            super(typeName, statement, parentName);
            addImplementsType(ParameterizedType.of(BindingTypes.JAVA_DATACONTAINER, TypeRef.of(typeName)));
        }

        @Override
        public ContainerObjectArchetype build() {
            return new ContainerObjectArchetypeImpl(typeName, statement, parentName, implementsTypes(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<ContainerObjectArchetype> archetypeClass() {
            return ContainerObjectArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final ContainerEffectiveStatement statement,
            final JavaTypeName parentName) {
        return new Builder(typeName, statement, parentName);
    }

    @Override
    ContainerEffectiveStatement statement();
}
