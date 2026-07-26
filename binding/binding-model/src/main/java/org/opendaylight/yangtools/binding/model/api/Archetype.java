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
public sealed interface Archetype extends Type
        permits Archetype.OfCompositeInterface, Archetype.WithQName, KeyArchetype, TypeObjectArchetype {
    /**
     * An {@link Archetype} which derives {@value Naming#QNAME_STATIC_FIELD_NAME} from its {@link #statement()}.
     *
     * @since 16.0.0
     */
    @Beta
    sealed interface WithQName extends Archetype
            permits ChoiceInArchetype, FeatureArchetype, IdentityArchetype, OpaqueObjectArchetype, RpcArchetype {
        @Override
        EffectiveStatement<QName, ?> statement();

        /**
         * {@return the value of {@value Naming#QNAME_STATIC_FIELD_NAME} field}
         */
        default @NonNull QName qnameConstant() {
            return statement().argument();
        }
    }

    /**
     * An {@link Archetype} which results in an interface with zero or more methods.
     *
     * @since 16.0.0
     */
    // FIXME: split out to InterfaceArchetype
    @Beta
    sealed interface OfCompositeInterface extends Archetype permits DataRootArchetype, LegacyArchetype {
        /**
         * {@return the list of annotations attached to interface declaration}
         */
        @NonNullByDefault
        List<AttachedAnnotation> annotations();

        /**
         * {@return the list of interfaces the interface extends}
         */
        @NonNullByDefault
        List<Type> getImplements();

        /**
         * {@return the list of constants the interface defines}
         */
        @NonNullByDefault
        List<Constant> getConstantDefinitions();

        /**
         * {@return the list of methods the interface defines}
         */
        @NonNullByDefault
        List<MethodSignature> getMethodDefinitions();
    }

    /**
     * {@return the {@link EffectiveStatement}}
     *
     * @since 16.0.0
     */
    @NonNull EffectiveStatement<?, ?> statement();

    /**
     * {@return the list of enclosed {@link Archetype}s}
     */
    @NonNullByDefault
    default List<Archetype> enclosedTypes() {
        return List.of();
    }
}
