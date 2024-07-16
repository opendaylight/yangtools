/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A archetype of a generated entity.
 */
@NonNullByDefault
public abstract sealed class Archetype<T extends Archetype<T>> implements Immutable
        permits ActionArchetype, AnnotationArchetype, DataContainerArchetype, FeatureArchetype, IdentityArchetype,
                KeyArchetype, OpaqueObjectArchetype, RpcArchetype, TypeObjectArchetype {
    private final JavaTypeName typeName;

    Archetype(final JavaTypeName typeName) {
        this.typeName = requireNonNull(typeName);
    }

    public final JavaTypeName typeName() {
        return typeName;
    }

    /**
     * Returns the archetype contract. This method ensures there is exactly one contract.
     *
     * @return the archetype contract
     */
    public abstract Class<T> archetypeContract();
}
