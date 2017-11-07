/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Contains methods for getting data from the <code>instance-identifier</code>
 * YANG built-in type.
 */
// FIXME: Something better than Object?
public interface InstanceIdentifierTypeDefinition extends TypeDefinition<InstanceIdentifierTypeDefinition, Object> {
    /**
     * Returns true|false which represents argument of <code>require-instance</code> statement. This statement is the
     * substatement of the <code>type</code> statement.
     *
     * @return boolean value which is true if the <code>require-instance</code> statement is true and vice versa
     */
    boolean requireInstance();

    static int hashCode(final InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.requireInstance());
    }

    static boolean equals(final InstanceIdentifierTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final InstanceIdentifierTypeDefinition other = TypeDefinitions.castIfEquals(
            InstanceIdentifierTypeDefinition.class, type, obj);
        return other != null && type.requireInstance() == other.requireInstance();
    }

    static String toString(final InstanceIdentifierTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("requireInstance", type.requireInstance()).toString();
    }
}
