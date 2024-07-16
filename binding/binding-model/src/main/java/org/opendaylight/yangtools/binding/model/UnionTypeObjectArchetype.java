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
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * An archetype for a {@link UnionTypeObject}.
 */
@NonNullByDefault
public record UnionTypeObjectArchetype(JavaTypeName typeName) implements TypeObjectArchetype<UnionTypeObjectArchetype> {
    public UnionTypeObjectArchetype {
        requireNonNull(typeName);
    }

    @Override
    public Class<UnionTypeObjectArchetype> archetypeContract() {
        return UnionTypeObjectArchetype.class;
    }
}
