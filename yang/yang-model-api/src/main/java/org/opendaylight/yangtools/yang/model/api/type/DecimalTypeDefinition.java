/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Contains methods for getting data from the YANG <code>type</code> substatement for <code>decimal64</code> built-in
 * type.
 */
public interface DecimalTypeDefinition extends RangeRestrictedTypeDefinition<DecimalTypeDefinition, BigDecimal> {
    /**
     * Returns integer between 1 and 18 inclusively.
     *
     * <p>
     * The "fraction-digits" statement controls the size of the minimum
     * difference between values of a decimal64 type, by restricting the value
     * space to numbers that are expressible as "i x 10^-n" where n is the
     * fraction-digits argument.
     *
     * @return number of fraction digits
     */
    int getFractionDigits();

    static int hashCode(final DecimalTypeDefinition type) {
        return Objects.hash(type.getQName(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getFractionDigits(),
            type.getRangeConstraint().orElse(null));
    }

    static boolean equals(final DecimalTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final DecimalTypeDefinition other = TypeDefinitions.castIfEquals(DecimalTypeDefinition.class, type, obj);
        return other != null && type.getFractionDigits() == other.getFractionDigits()
                && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static String toString(final DecimalTypeDefinition type) {
        return TypeDefinitions.toStringHelper(type).add("fractionDigits", type.getFractionDigits()).toString();
    }
}
