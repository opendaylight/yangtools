/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Enum Builder is interface that contains methods to build and instantiate Enumeration definition.
 *
 * @see Enumeration
 */
public interface EnumBuilder extends Type, AnnotableTypeBuilder {

    void setDescription(String description);

    Enumeration toInstance();

    /**
     * Updates this builder with data from <code>enumTypeDef</code>. Specifically this data represents list
     * of value-name pairs.
     *
     * @param enumTypeDef enum type definition as source of enum data for <code>enumBuilder</code>
     */
    void updateEnumPairsFromEnumTypeDef(EnumTypeDefinition enumTypeDef);
}
