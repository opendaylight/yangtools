/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import java.math.BigDecimal;

final class BigDecimalRangeGenerator extends AbstractRangeGenerator<BigDecimal> {
    BigDecimalRangeGenerator() {
        super(BigDecimal.class);
    }

    @Override
    protected boolean needsMaximumEnforcement(final BigDecimal maxToEnforce) {
        return true;
    }

    @Override
    protected String format(final BigDecimal number) {
        if (BigDecimal.ZERO.equals(number)) {
            return "java.math.BigDecimal.ZERO";
        }
        if (BigDecimal.ONE.equals(number)) {
            return "java.math.BigDecimal.ONE";
        }
        if (BigDecimal.TEN.equals(number)) {
            return "java.math.BigDecimal.TEN";
        }

        // FIXME: can we do something better?
        return "new java.math.BigDecimal(\"" + number.toString() + "\")";
    }

}
