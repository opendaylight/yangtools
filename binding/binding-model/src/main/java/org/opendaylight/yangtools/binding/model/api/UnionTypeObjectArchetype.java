/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Streams;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * An archetype for a {@link UnionTypeObject}.
 *
 * @param typePropertyNames list of property names corresponding to individual {@code type} statements within this
 *        union. The ordering of the returned list matches the ordering of the type statements.
 * @since 16.0.0
 */
// FIXME: should be an interface + implementation
@Beta
@NonNullByDefault
public record UnionTypeObjectArchetype(
        JavaTypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        List<String> typePropertyNames,
        List<Type> typePropertyTypes,
        // FIXME: YANGTOOLS-1621: not really, these should be the tag types
        List<TypeObjectArchetype<?>> enclosedTypes,
        @Nullable UnionTypeObjectArchetype getSuperType) implements TypeObjectArchetype.OfClass<UnionTypeObject> {
    /**
     * A tag in a union.
     *
     * @since 16.0.0
     */
    @Beta
    public record Tag(String name, Type type) {
        public Tag {
            requireNonNull(name);
            requireNonNull(type);
        }
    }

    public UnionTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        typePropertyNames = List.copyOf(typePropertyNames);
        typePropertyTypes = List.copyOf(typePropertyTypes);
        enclosedTypes = List.copyOf(enclosedTypes);

        final var uniqueNames = typePropertyNames.stream().distinct().count();
        if (uniqueNames != typePropertyTypes.size()) {
            throw new IllegalArgumentException(uniqueNames + " names does not match " + typePropertyTypes);
        }
    }

    /**
     * {@return all possible tags in the {@link UnionTypeObject}}
     */
    public List<Tag> tags() {
        return Streams.zip(typePropertyNames().stream().distinct(), typePropertyTypes().stream(), Tag::new).toList();
    }

    @Override
    @Deprecated(forRemoval = true)
    public @Nullable TypeDefinition<?> getBaseType() {
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
}
