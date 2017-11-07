/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

// FIXME: something better than Object
public interface LeafrefTypeDefinition extends TypeDefinition<LeafrefTypeDefinition, Object> {

    RevisionAwareXPath getPathStatement();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
     * LeafrefTypeDefinition which does not support require-instance statement.
     * YANG leafref type has been changed in YANG 1.1 (RFC7950) and now allows require-instance statement.
     *
     * @return boolean value which is true if the <code>require-instance</code> statement is true and vice versa
     */
     // FIXME: version 2.0.0: make this method non-default
    default boolean requireInstance() {
        return true;
    }

    static int hashCode(final LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getPathStatement());
    }


    static boolean equals(final LeafrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final LeafrefTypeDefinition other = TypeDefinitions.castIfEquals(LeafrefTypeDefinition.class, type, obj);
        return other != null && type.getPathStatement().equals(other.getPathStatement());
    }

    static String toString(final LeafrefTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("pathStatement", type.getPathStatement()).toString();
    }
}
