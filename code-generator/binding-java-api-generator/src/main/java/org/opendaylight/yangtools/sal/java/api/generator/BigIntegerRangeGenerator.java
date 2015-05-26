/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import java.math.BigInteger;

final class BigIntegerRangeGenerator extends AbstractBigRangeGenerator<BigInteger> {
    BigIntegerRangeGenerator() {
        super(BigInteger.class);
    }

    @Override
    protected String format(final BigInteger number) {
        if (BigInteger.ZERO.equals(number)) {
            return "java.math.BigInteger.ZERO";
        }
        if (BigInteger.ONE.equals(number)) {
            return "java.math.BigInteger.ONE";
        }
        if (BigInteger.TEN.equals(number)) {
            return "java.math.BigInteger.TEN";
        }

        // Check for conversion to long
        final long l = number.longValue();
        if (number.equals(BigInteger.valueOf(l))) {
            return "java.math.BigInteger.valueOf(" + l + "L)";
        } else {
            return "new java.math.BigInteger(\"" + number.toString() + "\")";
        }
    }

    @Override
    protected BigInteger convert(final Number value) {
        return BigInteger.valueOf(value.longValue());
    }
}
