/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversion constants for all scales supported by Decimal64.
 */
@NonNullByDefault
enum Decimal64Conversion {
    SCALE_1(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -922337203685477580L,   922337203685477580L,
            -922337203685477580.8F, 922337203685477580.7F,
            -922337203685477580.8D, 922337203685477580.7D),
    SCALE_2(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -92233720368547758L,    92233720368547758L,
            -92233720368547758.08F, 92233720368547758.07F,
            -92233720368547758.08F, 92233720368547758.07D),
    SCALE_3(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -9223372036854775L,     9223372036854775L,
            -9223372036854775.808F, 9223372036854775.807F,
            -9223372036854775.808D, 9223372036854775.807D),
    SCALE_4(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -922337203685477L,      922337203685477L,
            -922337203685477.5808F, 922337203685477.5807F,
            -922337203685477.5808D, 922337203685477.5807D),
    SCALE_5(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -92233720368547L,       92233720368547L,
            -92233720368547.75808F, 92233720368547.75807F,
            -92233720368547.75808D, 92233720368547.75807D),
    SCALE_6(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -9223372036854L,        9223372036854L,
            -9223372036854.775808F, 9223372036854.775807F,
            -9223372036854.775808D, 9223372036854.775807D),
    SCALE_7(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -922337203685L,         922337203685L,
            -922337203685.4775808F, 922337203685.4775807F,
            -922337203685.4775808D, 922337203685.4775807D),
    SCALE_8(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -92233720368L,          92233720368L,
            -92233720368.54775808F, 92233720368.54775807F,
            -92233720368.54775808D, 92233720368.54775807D),
    SCALE_9(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
            -9223372036L,           9223372036L,
            -9223372036.854775808F, 9223372036.854775807F,
            -9223372036.854775808D, 9223372036.854775807D),
    SCALE_10(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, -922337203, 922337203,
            -922337203L,            922337203L,
            -922337203.6854775808F, 922337203.6854775807F,
            -922337203.6854775808D, 922337203.6854775807D),
    SCALE_11(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, -92233720, 92233720,
            -92233720L,             92233720L,
            -92233720.36854775808F, 92233720.36854775807F,
            -92233720.36854775808D, 92233720.36854775807D),
    SCALE_12(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, -9223372, 9223372, -9223372L, 9223372L,
            -9223372.036854775808F, 9223372.036854775807F,
            -9223372.036854775808D, 9223372.036854775807D),
    SCALE_13(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, -922337, 922337, -922337L, 922337L,
            -922337.2036854775808F, 922337.2036854775807F,
            -922337.2036854775808D, 922337.2036854775807D),
    SCALE_14(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, -92233, 92233, -92233L, 92233L,
            -92233.72036854775808F, 92233.72036854775807F,
            -92233.72036854775808D, 92233.72036854775807D),
    SCALE_15(Byte.MIN_VALUE, Byte.MAX_VALUE, (short) -9223, (short) 9223, -9223, 9223, -9223L, 9223L,
            -9223.372036854775808F, 9223.372036854775807F,
            -9223.372036854775808D, 9223.372036854775807D),
    SCALE_16(Byte.MIN_VALUE, Byte.MAX_VALUE, (short) -922, (short) 922, -922, 922, -922L, 922L,
            -922.3372036854775808F, 922.3372036854775807F,
            -922.3372036854775808D, 922.3372036854775807D),
    SCALE_17((byte) -92, (byte)92, (short) -92, (short) 92, -92, 92, -92L, 92L,
            -92.23372036854775808F, 92.23372036854775807F,
            -92.23372036854775808D, 92.23372036854775807D),
    SCALE_18((byte) -9, (byte)9, (short) -9, (short) 9, -9, 9, -9L, 9L,
            -9.223372036854775808F, 9.223372036854775807F,
            -9.223372036854775808D, 9.223372036854775807D);

    final byte minByte;
    final byte maxByte;

    final short minShort;
    final short maxShort;

    final int minInt;
    final int maxInt;

    final long minLong;
    final long maxLong;

    final float minFloat;
    final float maxFloat;

    final double minDouble;
    final double maxDouble;

    Decimal64Conversion(
            final byte minByte, final byte maxByte,
            final short minShort, final short maxShort,
            final int minInt, final int maxInt,
            final long minLong, final long maxLong,
            final float minFloat, final float maxFloat,
            final double minDouble, final double maxDouble) {
        this.minByte = minByte;
        this.maxByte = maxByte;
        this.minShort = minShort;
        this.maxShort = maxShort;
        this.minInt = minInt;
        this.maxInt = maxInt;
        this.minLong = minLong;
        this.maxLong = maxLong;
        this.minFloat = minFloat;
        this.maxFloat = maxFloat;
        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
    }
}
