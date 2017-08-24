/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import javax.annotation.Nonnull;

/**
 * Contains methods for getting data from the YANG <code>type</code> substatement for <code>decimal64</code> built-in
 * type.
 */
public interface DecimalTypeDefinition extends RangeRestrictedTypeDefinition<DecimalTypeDefinition> {
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
    @Nonnull Integer getFractionDigits();
}
