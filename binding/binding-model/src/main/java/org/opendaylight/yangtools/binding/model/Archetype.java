/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A archetype of a generated entity.
 */
@NonNullByDefault
public sealed interface Archetype<T extends Archetype<T>> extends Immutable
        permits ActionArchetype, AnnotationArchetype, DataContainerArchetype, FeatureArchetype, IdentityArchetype,
                KeyArchetype, OpaqueObjectArchetype, RpcArchetype, TypeObjectArchetype {
    /**
     * Returns the archetype contract. This method ensures there is exactly one contract.
     *
     * @return the archetype contract
     */
    Class<T> archetypeContract();

    JavaTypeName typeName();
}
