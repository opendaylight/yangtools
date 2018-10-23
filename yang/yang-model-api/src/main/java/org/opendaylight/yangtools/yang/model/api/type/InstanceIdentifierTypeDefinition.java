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

/**
 * Contains methods for getting data from the <code>instance-identifier</code> YANG built-in type.
 */
public interface InstanceIdentifierTypeDefinition
        extends RequireInstanceRestrictedTypeDefinition<InstanceIdentifierTypeDefinition> {
    static int hashCode(final @NonNull InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.requireInstance());
    }

    static boolean equals(final @NonNull InstanceIdentifierTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final InstanceIdentifierTypeDefinition other = TypeDefinitions.castIfEquals(
            InstanceIdentifierTypeDefinition.class, type, obj);
        return other != null && type.requireInstance() == other.requireInstance();
    }

    static String toString(final @NonNull InstanceIdentifierTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("requireInstance", type.requireInstance()).toString();
    }
}
