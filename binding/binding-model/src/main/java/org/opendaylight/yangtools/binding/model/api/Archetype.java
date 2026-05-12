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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link GeneratedType} whose parameters are derived from a set of invariants.
 *
 * @since 15.0.0
 */
@Beta
public sealed interface Archetype extends GeneratedType, Immutable
        permits Archetype.WithStatement, TypeObjectArchetype {
    /**
     * An {@link Archetype} which is based on a particular {@link EffectiveStatement}.
     *
     * @param <S> EffectiveStatement type
     * @since 16.0.0
     */
    @Beta
    sealed interface WithStatement<S extends EffectiveStatement<?, ?>> extends Archetype
            permits Compat, DataRootArchetype {
        /**
         * {@return the {@link EffectiveStatement}}
         */
        @NonNull S statement();
    }

    /**
     * Compatibility {@link GeneratedType} implementing specified methods for archetypes which do not provide them
     * anymore.
     *
     * @param <S> EffectiveStatement type
     * @since 16.0.0
     */
    @Beta
    sealed interface Compat<S extends EffectiveStatement<?, ?>> extends WithStatement<S>
            permits WithQName, BitsTypeObjectArchetype, EnumTypeObjectArchetype, KeyArchetype {
        @Override
        @Deprecated(forRemoval = true)
        default String getDescription() {
            throw uoe();
        }

        @Override
        @Deprecated(forRemoval = true)
        default String getReference() {
            throw uoe();
        }

        @Override
        @Deprecated(forRemoval = true)
        default String getModuleName() {
            throw uoe();
        }

        @Override
        @Deprecated(forRemoval = true)
        default @Nullable TypeComment getComment() {
            throw uoe();
        }

        @Override
        @Deprecated(forRemoval = true)
        default @Nullable YangSourceDefinition yangSourceDefinition() {
            throw uoe();
        }

        private static UnsupportedOperationException uoe() {
            throw new UnsupportedOperationException("should never be called");
        }
    }

    /**
     * Compatibility {@link GeneratedType} method implementations for archetypes which do not provide them anymore.
     *
     * @param <S> EffectiveStatement type
     * @since 16.0.0
     */
    @Beta
    sealed interface WithQName<S extends EffectiveStatement<QName, ?>> extends Compat<S>
            permits ChoiceInArchetype, FeatureArchetype, IdentityArchetype, OpaqueObjectArchetype, RpcArchetype {
        /**
         * {@return the value of {@value Naming#QNAME_STATIC_FIELD_NAME} field}
         */
        default QName qnameConstant() {
            return statement().argument();
        }
    }
}
