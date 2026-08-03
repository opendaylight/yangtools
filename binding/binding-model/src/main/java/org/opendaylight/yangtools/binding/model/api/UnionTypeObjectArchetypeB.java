/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

@NonNullByDefault
record UnionTypeObjectArchetypeB(
        TypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        List<String> typePropertyNames,
        List<Type> typePropertyTypes,
        List<TypeObjectArchetype<?>> enclosedTypes) implements UnionTypeObjectArchetype {
    UnionTypeObjectArchetypeB {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typePropertyNames);
        requireNonNull(typePropertyTypes);
        requireNonNull(enclosedTypes);
    }

    @Override
    public @Nullable UnionTypeObjectArchetype superType() {
        return null;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeMethods.toString(this);
    }
}
