/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * An archetype for a {@link TypeObject} specialization.
 */
@NonNullByDefault
public abstract sealed class TypeObjectArchetype<T extends TypeObjectArchetype<T>> extends Archetype<T>
        permits BitsTypeObjectArchetype, EnumTypeObjectArchetype, ScalarTypeObjectArchetype, UnionTypeObjectArchetype {
    TypeObjectArchetype(final JavaTypeName typeName) {
        super(typeName);
    }
}
