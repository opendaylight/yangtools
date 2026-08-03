/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Streams;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.impl.UnionTypeObjectArchetypeB;
import org.opendaylight.yangtools.binding.model.impl.UnionTypeObjectArchetypeD;
import org.opendaylight.yangtools.binding.model.impl.UnionTypeObjectArchetypeW;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * An archetype for a {@link UnionTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface UnionTypeObjectArchetype extends TypeObjectArchetype.OfClass<UnionTypeObject>
        permits UnionTypeObjectArchetypeB, UnionTypeObjectArchetypeD, UnionTypeObjectArchetypeW {
    /**
     * A tag in a union.
     *
     * @since 16.0.0
     */
    @Beta
    record Tag(String name, Type type) {
        public Tag {
            requireNonNull(name);
            requireNonNull(type);
        }
    }

    @Override
    @Nullable UnionTypeObjectArchetype superType();

    @Override
    @Deprecated(forRemoval = true)
    default @Nullable TypeDefinition<?> baseType() {
        return null;
    }

    // FIXME: YANGTOOLS-1621: not really, these should be the tag types, at which point we will remove typeProperties()
    List<TypeObjectArchetype<?>> enclosedTypes();

    List<String> typePropertyNames();

    List<Type> typePropertyTypes();

    /**
     * {@return all possible tags in the {@link UnionTypeObject}}
     */
    default List<Tag> tags() {
        return Streams.zip(typePropertyNames().stream().distinct(), typePropertyTypes().stream(), Tag::new).toList();
    }

    static UnionTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final List<String> typePropertyNames, final List<Type> typePropertyTypes,
            final List<TypeObjectArchetype<?>> enclosedTypes) {
        final var uniqueNames = typePropertyNames.stream().distinct().count();
        if (uniqueNames != typePropertyTypes.size()) {
            throw new IllegalArgumentException(uniqueNames + " names does not match " + typePropertyTypes);
        }
        return new UnionTypeObjectArchetypeB(name, statement, List.copyOf(typePropertyNames),
            List.copyOf(typePropertyTypes), List.copyOf(enclosedTypes));
    }

    static UnionTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final UnionTypeObjectArchetype superType) {
        return new UnionTypeObjectArchetypeD(name, statement, superType);
    }

    static UnionTypeObjectArchetype of(final UnionTypeObjectArchetype union) {
        return new UnionTypeObjectArchetypeW(union);
    }
}
