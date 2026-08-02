/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link Type} representing a Java class generated for a YANG statement.
 *
 * @since 15.0.0
 */
public sealed interface Archetype extends Type
        permits ChoiceInArchetype, DataContainerArchetype, FeatureArchetype, IdentityArchetype, OperationArchetype,
                KeyArchetype, OpaqueObjectArchetype, TypeObjectArchetype {
    /**
     * {@return the {@link EffectiveStatement} from which the class was generated}
     *
     * @since 16.0.0
     */
    @NonNull EffectiveStatement<?, ?> statement();

    /**
     * {@return the list of enclosed {@link Archetype}s}
     */
    // FIXME: needs to be two separate contracts:
    //        - List<TypeObjectArchetype> in DataContainerArchetype
    //        - List<UnionTypeObject.Member> in UnionTypeObjectArchetype
    //          - TypeObjectArchetype is Member for now, but will get split with YANGTOOLS-1611
    @NonNullByDefault
    default List<? extends Archetype> enclosedTypes() {
        return List.of();
    }
}
