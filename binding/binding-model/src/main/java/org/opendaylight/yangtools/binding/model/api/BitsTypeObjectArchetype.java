/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * An archetype for a {@link BitsTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface BitsTypeObjectArchetype extends TypeObjectArchetype.OfClass<BitsTypeObject>
        permits BitsTypeObjectArchetypeB, BitsTypeObjectArchetypeD {
    @Override
    @Nullable BitsTypeObjectArchetype superType();

    @Override
    @Deprecated(forRemoval = true)
    default @NonNull BitsTypeDefinition baseType() {
        return typeDefinition();
    }

    /**
     * {@return the {@link BitsTypeDefinition} of this type}
     */
    BitsTypeDefinition typeDefinition();

    static BitsTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final BitsTypeDefinition typeDefinition) {
        return new BitsTypeObjectArchetypeB(name, statement, typeDefinition);
    }

    static BitsTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final BitsTypeDefinition typeDefinition, final @Nullable BitsTypeObjectArchetype superType) {
        return superType == null ? of(name, statement, typeDefinition)
            : new BitsTypeObjectArchetypeD(name, statement, typeDefinition, superType);
    }
}
