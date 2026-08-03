/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * An archetype for a {@link ScalarTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface ScalarTypeObjectArchetype extends TypeObjectArchetype.OfClass<ScalarTypeObject<?>>
        permits ScalarTypeObjectArchetypeB, ScalarTypeObjectArchetypeBR, ScalarTypeObjectArchetypeD,
                ScalarTypeObjectArchetypeDR {
    @Override
    @Nullable ScalarTypeObjectArchetype superType();

    @Override
    @Deprecated(forRemoval = true)
    default @NonNull TypeDefinition<?> baseType() {
        return typeDefinition();
    }

    /**
     * {@return the {@link TypeDefinition} of this type}
     */
    TypeDefinition<?> typeDefinition();

    /**
     * {@return the {@link ConcreteType} of the type returned by {@link ScalarTypeObject#getValue()}}
     */
    ConcreteType valueType();

    /**
     * {@return the {@link Restrictions} of this type}
     */
    @Nullable Restrictions restrictions();

    static ScalarTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final TypeDefinition<?> typeDefinition, final ConcreteType valueType) {
        return new ScalarTypeObjectArchetypeB(name, statement, typeDefinition, valueType);
    }

    static ScalarTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final TypeDefinition<?> typeDefinition, final ConcreteType valueType,
            final @Nullable Restrictions restrictions) {
        return restrictions == null || restrictions.isEmpty() ? of(name, statement, typeDefinition, valueType)
            : new ScalarTypeObjectArchetypeBR(name, statement, typeDefinition, valueType, restrictions);
    }

    static ScalarTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final TypeDefinition<?> typeDefinition, final ConcreteType valueType,
            final @Nullable ScalarTypeObjectArchetype superType) {
        return superType == null ? of(name, statement, typeDefinition, valueType)
            : new ScalarTypeObjectArchetypeD(name, statement, typeDefinition, valueType, superType);
    }

    static ScalarTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final TypeDefinition<?> typeDefinition, final ConcreteType valueType,
            final @Nullable Restrictions restrictions, final @Nullable ScalarTypeObjectArchetype superType) {
        if (restrictions == null || restrictions.isEmpty()) {
            return of(name, statement, typeDefinition, valueType, superType);
        }
        return superType == null ? of(name, statement, typeDefinition, valueType, restrictions)
            : new ScalarTypeObjectArchetypeDR(name, statement, typeDefinition, valueType, restrictions, superType);
    }
}
