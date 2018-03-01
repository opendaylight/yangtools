/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import java.math.BigInteger;

final class BigIntegerRangeGenerator extends AbstractBigRangeGenerator<BigInteger> {
    BigIntegerRangeGenerator() {
        super(BigInteger.class);
    }

    @Override
    protected String format(final BigInteger value) {
        if (BigInteger.ZERO.equals(value)) {
            return "java.math.BigInteger.ZERO";
        }
        if (BigInteger.ONE.equals(value)) {
            return "java.math.BigInteger.ONE";
        }
        if (BigInteger.TEN.equals(value)) {
            return "java.math.BigInteger.TEN";
        }

        // Check for conversion to long
        final long l = value.longValue();
        if (value.equals(BigInteger.valueOf(l))) {
            return "java.math.BigInteger.valueOf(" + l + "L)";
        }

        return "new java.math.BigInteger(\"" + value.toString() + "\")";
    }

    @Override
    @Deprecated
    protected BigInteger convert(final Number value) {
        return BigInteger.valueOf(value.longValue());
    }
}
