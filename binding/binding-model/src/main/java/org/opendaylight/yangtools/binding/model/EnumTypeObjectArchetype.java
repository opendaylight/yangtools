/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.impl.EnumTypeObjectArchetypeImpl;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * The {@link Archetype} for {@link EnumTypeObject} specializations.
 *
 * @since 15.0.0
 */
@Beta
@NonNullByDefault
public sealed interface EnumTypeObjectArchetype extends TypeObjectArchetype<EnumTypeObject>
        permits EnumTypeObjectArchetypeImpl {

    EnumTypeDefinition typeDefinition();

    /**
     * {@return the injective mapping from YANG {@code enum} assigned name to its assigned Java {@code enum} constant,
     * with iteration order matching {@code typeDefinition().getValues()}}
     */
    BiMap<EnumPair, String> valueToConstant();

    static EnumTypeObjectArchetype of(final TypeName name, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final EnumTypeDefinition typeDefinition) {
        return new EnumTypeObjectArchetypeImpl(name, statement, typeDefinition);
    }
}
