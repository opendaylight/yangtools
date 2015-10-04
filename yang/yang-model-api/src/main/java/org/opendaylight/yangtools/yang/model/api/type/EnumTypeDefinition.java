/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *
 * Makes is possible to access to the individual enumeration values of this
 * type.
 *
 */
public interface EnumTypeDefinition extends TypeDefinition<EnumTypeDefinition> {

    /**
     * Returns all enumeration values.
     *
     * @return list of <code>EnumPair</code> type instastances which contain the
     *         data about all individual enumeration pairs of
     *         <code>enumeration</code> YANG built-in type
     */
    List<EnumPair> getValues();

    /**
     *
     * Contains the methods for accessing the data about the concrete
     * enumeration item which represents <code>enum</code> YANG type.
     */
    interface EnumPair extends SchemaNode {
        /**
         * The name to specify each assigned name of an enumeration type.
         *
         * @return name of each assigned name of an enumeration type.
         */
        String getName();

        /**
         * The "value" statement, which is optional, is used to associate an
         * integer value with the assigned name for the enum. This integer value
         * MUST be in the range -2147483648 to 2147483647, and it MUST be unique
         * within the enumeration type.
         *
         * @return integer value assigned to enumeration
         */
        Integer getValue();
    }
}
