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
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

@NonNullByDefault
record UnionTypeObjectArchetypeD(
        JavaTypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        UnionTypeObjectArchetype superType) implements UnionTypeObjectArchetype {
    UnionTypeObjectArchetypeD {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(superType);
    }

    @Override
    public @NonNull UnionTypeObjectArchetype superType() {
        return superType;
    }

    @Override
    public List<String> typePropertyNames() {
        return List.of();
    }

    @Override
    public List<Type> typePropertyTypes() {
        return List.of();
    }

    @Override
    public List<Map.Entry<String, Type>> typeProperties() {
        return List.of();
    }

    @Override
    public List<TypeObjectArchetype<?>> enclosedTypes() {
        return List.of();
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
