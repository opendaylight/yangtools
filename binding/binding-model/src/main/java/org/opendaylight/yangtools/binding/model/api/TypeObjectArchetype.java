/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * An {@link Archetype} for one of the four {@link TypeObject} specializations.
 *
 * @param <T> the {@link TypeObject} specialization
 * @since 16.0.0
 */
@Beta
public sealed interface TypeObjectArchetype<T extends TypeObject>
    // FIXME: this is not entirely accurate: we want to have:
    //        - TypeEffectiveStatement statement()
    //        - TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement();
        extends Archetype.WithStatement<TypeEffectiveStatement.@NonNull MandatoryIn<?, ?>>
        permits EnumTypeObjectArchetype, TypeObjectArchetype.OfClass {
    /**
     * Common interface for {@link TypeObjectArchetype}s other than {@link EnumTypeObjectArchetype}. These archetypes
     * result in a non-abstract class, which is potentially a subclass of another class of the same kind.
     *
     * @param <T> {@link TypeObject} specialization
     * @since 16.0.0
     */
    sealed interface OfClass<T extends TypeObject> extends TypeObjectArchetype<T>
            permits BitsTypeObjectArchetype, ScalarTypeObjectArchetype, UnionTypeObjectArchetype {
        /**
         * {@return the value of the {@code serialVersionUID} of this {@link TypeObject} class}
         */
        long serialVersionUID();

        /**
         * {@return the archetype describing the class this archetype's class extends, or {@code null}}
         */
        @Nullable OfClass<T> getSuperType();

        // FIXME: why do we need this boolean?
        @Deprecated(since = "16.0.0", forRemoval = true)
        default boolean isTypedef() {
            return statement() instanceof TypedefEffectiveStatement;
        }

        /**
         * {@return Base type of Java representation of YANG typedef if set, otherwise it returns {@code null}}
         */
        @Nullable TypeDefinition<?> getBaseType();
    }
}
