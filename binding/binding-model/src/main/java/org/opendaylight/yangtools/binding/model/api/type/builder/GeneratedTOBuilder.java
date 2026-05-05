/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTOBuilder;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Generated Transfer Object Builder is interface that contains methods to build
 * and instantiate Generated Transfer Object definition.
 *
 * @see GeneratedTransferObject
 */
public sealed interface GeneratedTOBuilder extends GeneratedTypeBuilderBase<GeneratedTOBuilder>
        permits AbstractGeneratedTOBuilder, BitsTypeObjectArchetype.Builder, ScalarTypeObjectArchetype.Builder,
                UnionTypeObjectArchetype.Builder {
    /**
     * Add Generated Transfer Object from which will be extended current Generated Transfer Object.<br>
     * By definition Java does not allow multiple inheritance, hence if there is already a definition
     * of an Generated Transfer Object the extending object will be overwritten by lastly added Generated Transfer
     * Object.<br>
     * If Generated Transfer Object is <code>null</code> the method SHOULD throw {@link IllegalArgumentException}
     *
     * @param genTransObj Generated Transfer Object
     * @return This instance of builder
     */
    GeneratedTOBuilder setExtendsType(GeneratedTransferObject<?> genTransObj);

    @NonNullByDefault
    void setRestrictions(Restrictions restrictions);

    void setTypedef(boolean isTypedef);

    /**
     * Sets the base type for Java representation of YANG typedef.
     *
     * @param typeDef Type Definition
     */
    void setBaseType(TypeDefinition<?> typeDef);

    /**
     * Returns instance of <code>GeneratedTransferObject</code> which data are build from the data of this builder.
     *
     * @return generated transfer object instance
     */
    @Override
    GeneratedTransferObject<?> build();
}
