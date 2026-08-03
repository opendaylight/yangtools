/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.model.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

@NonNullByDefault
public record ScalarTypeObjectArchetypeD(
        TypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        TypeDefinition<?> typeDefinition,
        ConcreteType valueType,
        ScalarTypeObjectArchetype superType) implements ScalarTypeObjectArchetype {
    public ScalarTypeObjectArchetypeD {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
        requireNonNull(valueType);
        requireNonNull(superType);
    }

    @Override
    public @NonNull ScalarTypeObjectArchetype superType() {
        return superType;
    }

    @Override
    public @Nullable Restrictions restrictions() {
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
    public final String toString() {
        return TypeMethods.toString(this);
    }
}
