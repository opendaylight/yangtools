/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Makes is possible to access to the individual enumeration values of this type.
 */
public interface EnumTypeDefinition extends TypeDefinition<EnumTypeDefinition> {
    /**
     * Contains the methods for accessing the data about the concrete enumeration item which represents {@code enum}
     * YANG type.
     */
    interface EnumPair extends DocumentedNode.WithStatus {
        /**
         * The name to specify each assigned name of an enumeration type.
         *
         * @return name of each assigned name of an enumeration type.
         */
        String getName();

        /**
         * The "value" statement, which is optional, is used to associate an integer value with the assigned name
         * for the enum. This integer value MUST be unique within the enumeration type.
         *
         * @return integer value assigned to enumeration
         */
        int getValue();
    }

    /**
     * Returns all enumeration values.
     *
     * @return list of {@code EnumPair} type instances which contain the data about all individual enumeration pairs
     *         of {@code enumeration} YANG built-in type
     */
    @NonNull List<EnumPair> getValues();

    static boolean equals(final EnumTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final EnumTypeDefinition other = TypeDefinitions.castIfEquals(EnumTypeDefinition.class, type, obj);
        return other != null && type.getValues().equals(other.getValues());
    }

    static int hashCode(final EnumTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null),
            type.getDefaultValue(), type.getValues());
    }

    static String toString(final EnumTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("values", type.getValues()).toString();
    }
}
