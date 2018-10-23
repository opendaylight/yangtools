/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

public interface LeafrefTypeDefinition extends RequireInstanceRestrictedTypeDefinition<LeafrefTypeDefinition> {

    // FIXME: this is not the same syntax as when statement. See https://tools.ietf.org/html/rfc7950#section-9.9.2
    RevisionAwareXPath getPathStatement();

    /**
     * {@inheritDoc}
     *
     * <p>
     * For YANG version 1 (RFC6020), this should always return true.
     */
    @Override
    boolean requireInstance();

    static int hashCode(final @NonNull LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getPathStatement());
    }

    static boolean equals(final @NonNull LeafrefTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final LeafrefTypeDefinition other = TypeDefinitions.castIfEquals(LeafrefTypeDefinition.class, type, obj);
        return other != null && type.getPathStatement().equals(other.getPathStatement());
    }

    static String toString(final @NonNull LeafrefTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("pathStatement", type.getPathStatement()).toString();
    }
}
