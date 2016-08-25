/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;
import javax.annotation.Nonnull;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Makes is possible to access to the individual bits values of this type.
 */
@Value.Immutable
public interface BitsTypeDefinition extends TypeDefinition<BitsTypeDefinition> {
    /**
     * Returns all bit values.
     *
     * @return list of <code>Bit</code> type instastances with data about all
     *         individual bits of <code>bits</code> YANG built-in type
     */
    @Nonnull List<Bit> getBits();

    /**
     *
     * Contains the methods for accessing the data about the individual bit of
     * <code>bits</code> YANG type.
     */
    @Value.Immutable
    interface Bit extends SchemaNode {
        /**
         * Returns the name of the concrete bit.
         *
         * @return string with the name of the concrete bit
         */
        @Nonnull String getName();

        /**
         * The position value MUST be in the range 0 to 4294967295, and it MUST
         * be unique within the bits type.
         *
         * @return The position value of bit in range from 0 to 4294967295.
         */
        long getPosition();
    }
}
