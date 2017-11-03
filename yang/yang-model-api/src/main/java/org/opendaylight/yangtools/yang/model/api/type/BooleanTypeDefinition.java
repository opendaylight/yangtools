/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Marker interface which marks that type definition represents the built-in
 * YANG <code>boolean</code> type.
 */
public interface BooleanTypeDefinition extends TypeDefinition<BooleanTypeDefinition> {

    static int hashCode(final BooleanTypeDefinition type) {
        return TypeDefinitions.basicHashCode(type);
    }

    static boolean equals(final BooleanTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return TypeDefinitions.castIfEquals(BooleanTypeDefinition.class, type, obj) != null;
    }

    static String toString(final BooleanTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).toString();
    }
}
