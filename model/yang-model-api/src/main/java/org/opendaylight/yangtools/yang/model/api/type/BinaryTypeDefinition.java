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
 * The binary built-in type represents any binary data, i.e., a sequence of octets. Binary values are encoded with the
 * Base64 encoding scheme (see <a href="https://www.rfc-editor.org/rfc/rfc4648#section-4">[RFC4648], Section 4</a>).<br>
 * The canonical form of a binary value follows the rules defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc4648">[RFC4648]</a>.
 *
 * <p>This interface was modeled according to definition in
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-9.8">[RFC-6020] The binary Built-In Type</a>
 */
public interface BinaryTypeDefinition extends LengthRestrictedTypeDefinition<BinaryTypeDefinition> {
    static String toString(final @NonNull BinaryTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("length", type.getLengthConstraint().orElse(null)).toString();
    }

    static int hashCode(final @NonNull BinaryTypeDefinition type) {
        return Objects.hash(type.getQName(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getLengthConstraint().orElse(null));
    }

    static boolean equals(final @NonNull BinaryTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final BinaryTypeDefinition other = TypeDefinitions.castIfEquals(BinaryTypeDefinition.class, type, obj);
        return other != null && type.getLengthConstraint().equals(other.getLengthConstraint());
    }
}
