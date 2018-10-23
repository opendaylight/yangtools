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

/**
 * Contains method for getting data from the <code>string</code> YANG built-in type.
 */
public interface StringTypeDefinition extends LengthRestrictedTypeDefinition<StringTypeDefinition> {
    /**
     * Returns patterns specified in the string.
     *
     * @return list of pattern constraints which are specified in the {@code pattern} substatement of the {@code type}
     *         statement
     */
    @NonNull List<PatternConstraint> getPatternConstraints();

    static int hashCode(final @NonNull StringTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getLengthConstraint().orElse(null),
            type.getPatternConstraints());
    }

    static boolean equals(final @NonNull StringTypeDefinition type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final StringTypeDefinition other = TypeDefinitions.castIfEquals(StringTypeDefinition.class, type, obj);
        return other != null && type.getLengthConstraint().equals(other.getLengthConstraint())
                && type.getPatternConstraints().equals(other.getPatternConstraints());
    }

    static String toString(final @NonNull StringTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("length", type.getLengthConstraint().orElse(null))
                .add("patterns", type.getPatternConstraints()).toString();
    }
}
