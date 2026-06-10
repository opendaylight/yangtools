/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link Type} representing a Java class from a set of invariants.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface Archetype extends Type permits Archetype.WithStatement, LegacyArchetype, TypeObjectArchetype {
    /**
     * An {@link Archetype} which is based on a particular {@link EffectiveStatement}.
     *
     * @param <S> EffectiveStatement type
     * @since 16.0.0
     */
    sealed interface WithStatement<S extends EffectiveStatement<?, ?>> extends Archetype
            permits WithQName, BitsTypeObjectArchetype, EnumTypeObjectArchetype, KeyArchetype,
                    ScalarTypeObjectArchetype, UnionTypeObjectArchetype, DataRootArchetype {
        /**
         * {@return the {@link EffectiveStatement}}
         */
        @NonNull S statement();
    }

    /**
     * An {@link Archetype} which derives {@value Naming#QNAME_STATIC_FIELD_NAME} from its {@link #statement()}.
     *
     * @param <S> EffectiveStatement type
     * @since 16.0.0
     */
    @Beta
    sealed interface WithQName<S extends EffectiveStatement<QName, ?>> extends WithStatement<S>
            permits ChoiceInArchetype, FeatureArchetype, IdentityArchetype, OpaqueObjectArchetype, RpcArchetype {
        /**
         * {@return the value of {@value Naming#QNAME_STATIC_FIELD_NAME} field}
         */
        default QName qnameConstant() {
            return statement().argument();
        }
    }

    /**
     * {@return the list of enclosed {@link Archetype}s}
     */
    default List<Archetype> enclosedTypes() {
        return List.of();
    }
}
