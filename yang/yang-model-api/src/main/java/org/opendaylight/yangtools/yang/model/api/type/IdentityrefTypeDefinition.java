/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Contains method for getting data from <code>identityref</code> built-in YANG type.
 */
public interface IdentityrefTypeDefinition extends TypeDefinition<IdentityrefTypeDefinition> {
    /**
     * Returns the set of identities this reference points to.
     *
     * @return set of identities to which the instance of this type refers (in YANG 1.1 models) or a set containing
     *         just one identity (in YANG 1.0 models)
     */
    @NonNull Set<IdentitySchemaNode> getIdentities();

    static int hashCode(final @NonNull IdentityrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getIdentities());
    }

    static boolean equals(final @NonNull IdentityrefTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final IdentityrefTypeDefinition other = TypeDefinitions.castIfEquals(IdentityrefTypeDefinition.class, type,
            obj);
        return other != null && type.getIdentities().equals(other.getIdentities());
    }

    static String toString(final @NonNull IdentityrefTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("identities", type.getIdentities()).toString();
    }
}
