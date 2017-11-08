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

public interface LeafrefTypeDefinition extends TypeDefinition<LeafrefTypeDefinition> {

    // FIXME: this is not the same syntax as when statement. See https://tools.ietf.org/html/rfc7950#section-9.9.2
    RevisionAwareXPath getPathStatement();

    /**
     * Require instance pointed to by {@link #getPathStatement()} to be present or not. For YANG version (RFC6020), this
     * should always return true.
     *
     * @return True if the instance pointed to by {@link #getPathStatement()} must be present in the data tree.
     */
    boolean requireInstance();

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
