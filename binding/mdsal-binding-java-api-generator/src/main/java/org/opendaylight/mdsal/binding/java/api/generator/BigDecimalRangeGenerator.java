/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import java.math.BigDecimal;
import java.math.BigInteger;

final class BigDecimalRangeGenerator extends AbstractBigRangeGenerator<BigDecimal> {
    BigDecimalRangeGenerator() {
        super(BigDecimal.class);
    }

    @Override
    protected String format(final BigDecimal value) {
        if (BigDecimal.ZERO.equals(value)) {
            return "java.math.BigDecimal.ZERO";
        }
        if (BigDecimal.ONE.equals(value)) {
            return "java.math.BigDecimal.ONE";
        }
        if (BigDecimal.TEN.equals(value)) {
            return "java.math.BigDecimal.TEN";
        }

        // FIXME: can we do something better?
        return "new java.math.BigDecimal(\"" + value + "\")";
    }

    @Override
    @Deprecated
    protected BigDecimal convert(final Number value) {
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger)value);
        } else if (value instanceof Byte) {
            return new BigDecimal(value.intValue());
        } else if (value instanceof Short) {
            return new BigDecimal(value.intValue());
        } else if (value instanceof Integer) {
            return new BigDecimal(value.intValue());
        } else {
            return BigDecimal.valueOf(value.longValue());
        }
    }
}
