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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Makes is possible to access to the individual bits values of this type.
 */
public interface BitsTypeDefinition extends TypeDefinition<BitsTypeDefinition> {
    /**
     * Returns all bit values.
     *
     * @return list of <code>Bit</code> type instastances with data about all
     *         individual bits of <code>bits</code> YANG built-in type
     */
    @NonNull List<Bit> getBits();

    static int hashCode(final @NonNull BitsTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getBits());
    }

    static boolean equals(final @NonNull BitsTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final BitsTypeDefinition other = TypeDefinitions.castIfEquals(BitsTypeDefinition.class, type, obj);
        return other != null && type.getBits().equals(other.getBits());
    }

    static String toString(final @NonNull BitsTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("bits", type.getBits()).toString();
    }

    /**
     * Contains the methods for accessing the data about the individual bit of
     * <code>bits</code> YANG type.
     */
    interface Bit extends SchemaNode {
        /**
         * Returns the name of the concrete bit.
         *
         * @return string with the name of the concrete bit
         */
        @NonNull String getName();

        /**
         * The position value MUST be in the range 0 to 4294967295, and it MUST
         * be unique within the bits type.
         *
         * @return The position value of bit in range from 0 to 4294967295.
         */
        long getPosition();
    }
}
