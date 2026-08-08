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
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * An archetype for a {@link ScalarTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
public record ScalarTypeObjectArchetype(
        @NonNull TypeName name,
        TypeEffectiveStatement.@NonNull MandatoryIn<?, ?> statement,
        @NonNull TypeDefinition<?> typeDefinition,
        @NonNull ConcreteType valueType,
        @Nullable Restrictions restrictions,
        @Nullable ScalarTypeObjectArchetype getSuperType) implements TypeObjectArchetype.OfClass<ScalarTypeObject<?>> {
    public ScalarTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
        requireNonNull(valueType);
        if (restrictions != null && restrictions.isEmpty()) {
            restrictions = null;
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public @NonNull TypeDefinition<?> getBaseType() {
        return typeDefinition;
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
        final var local = getSuperType;
        if (local != null) {
            helper.add("extends", local.name);
        }
        return helper.toString();
    }
}
