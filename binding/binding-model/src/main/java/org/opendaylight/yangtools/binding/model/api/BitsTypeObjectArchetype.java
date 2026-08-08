/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * An archetype for a {@link BitsTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public record BitsTypeObjectArchetype(
        TypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        BitsTypeDefinition typeDefinition,
        @Nullable BitsTypeObjectArchetype superType) implements TypeObjectArchetype.OfClass<BitsTypeObject> {
    public BitsTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
    }

    public BitsTypeObjectArchetype(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final BitsTypeDefinition typeDefinition) {
        this(name, statement, typeDefinition, null);
    }

    @Override
    @Deprecated(forRemoval = true)
    public @NonNull BitsTypeDefinition getBaseType() {
        return typeDefinition;
    }

    @Override
    @Deprecated(forRemoval = true)
    public @Nullable BitsTypeObjectArchetype getSuperType() {
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
        final var helper = MoreObjects.toStringHelper(this).add("name", name).add("type", typeDefinition);
        final var local = superType;
        if (local != null) {
            helper.add("extends", local.name);
        }
        return helper.toString();
    }
}
