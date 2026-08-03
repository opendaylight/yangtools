/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

@NonNullByDefault
public record BitsTypeObjectArchetypeD(
        TypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        BitsTypeDefinition typeDefinition,
        BitsTypeObjectArchetype superType) implements BitsTypeObjectArchetype {
    public BitsTypeObjectArchetypeD {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
        requireNonNull(superType);
    }

    @Override
    public @NonNull BitsTypeObjectArchetype superType() {
        return superType;
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
    public final String toString() {
        return TypeMethods.toString(this);
    }
}
