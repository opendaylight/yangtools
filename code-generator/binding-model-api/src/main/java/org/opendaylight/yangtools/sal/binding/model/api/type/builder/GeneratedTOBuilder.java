/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api.type.builder;

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Generated Transfer Object Builder is interface that contains methods to build
 * and instantiate Generated Transfer Object definition.
 *
 * @see GeneratedTransferObject
 */
public interface GeneratedTOBuilder extends GeneratedTypeBuilderBase<GeneratedTOBuilder> {

    /**
     * Add Generated Transfer Object from which will be extended current
     * Generated Transfer Object. <br>
     * By definition Java does not allow multiple inheritance, hence if there is
     * already definition of Generated Transfer Object the extending object will
     * be overwritten by lastly added Generated Transfer Object. <br>
     * If Generated Transfer Object is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}
     *
     * @param genTransObj
     *            Generated Transfer Object
     * @return This instance of builder
     */
    GeneratedTOBuilder setExtendsType(final GeneratedTransferObject genTransObj);

    /**
     * Add Property that will be part of <code>equals</code> definition. <br>
     * If Generated Property Builder is <code>null</code> the method SHOULD
     * throw {@link IllegalArgumentException}
     *
     * @param property
     *            Generated Property Builder
     * @return This instance of builder
     */
    GeneratedTOBuilder addEqualsIdentity(final GeneratedPropertyBuilder property);

    /**
     * Add Property that will be part of <code>hashCode</code> definition. <br>
     * If Generated Property Builder is <code>null</code> the method SHOULD
     * throw {@link IllegalArgumentException}
     *
     * @param property
     *            Generated Property Builder
     * @return This instance of builder
     */
    GeneratedTOBuilder addHashIdentity(final GeneratedPropertyBuilder property);

    /**
     * Add Property that will be part of <code>toString</code> definition. <br>
     * If Generated Property Builder is <code>null</code> the method SHOULD
     * throw {@link IllegalArgumentException}
     *
     * @param property
     *            Generated Property Builder
     * @return This instance of builder
     */
    GeneratedTOBuilder addToStringProperty(final GeneratedPropertyBuilder property);

    void setRestrictions(Restrictions restrictions);

    /**
     * Returns instance of <code>GeneratedTransferObject</code> which data are
     * build from the data of this builder
     *
     * @return generated transfer object instance
     */
    GeneratedTransferObject toInstance();

    void setTypedef(boolean isTypedef);

    /**
     * Sets the base type for Java representation of YANG typedef
     *
     * @param typeDef
     *           Type Definition
     */
    void setBaseType(TypeDefinition<?> typeDef);

    /**
     *
     * @param isUnion
     */
    void setIsUnion(boolean isUnion);

    void setIsUnionBuilder(boolean isUnionTypeBuilder);

    void setSUID(GeneratedPropertyBuilder suid);
}
