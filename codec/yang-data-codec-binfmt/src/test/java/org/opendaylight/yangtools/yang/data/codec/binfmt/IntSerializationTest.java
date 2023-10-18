/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IntSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testByte(final NormalizedNodeStreamVersion version, final int expectedByte, final int expectedByteOne) {
        assertSame(version, (byte) 0, expectedByte);
        assertSame(version, (byte) 1, expectedByteOne);
        assertSame(version, Byte.MAX_VALUE, expectedByteOne);
    }

    static List<Arguments> testByte() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96, 97));
    }

    @ParameterizedTest
    @MethodSource
    void testShort(final NormalizedNodeStreamVersion version, final int expectedShort, final int expectedShortOne) {
        assertSame(version, (short) 0, expectedShort);
        assertSame(version, (short) 1, expectedShortOne);
        assertSame(version, Short.MAX_VALUE, expectedShortOne);
    }

    static List<Arguments> testShort() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96, 98));
    }

    @ParameterizedTest
    @MethodSource
    void testInt(final NormalizedNodeStreamVersion version, final int expectedInt, final int expectedIntOne,
            final int expectedIntMax) {
        assertSame(version, 0, expectedInt);
        assertSame(version, 1, expectedIntOne);
        assertSame(version, Integer.MAX_VALUE, expectedIntMax);
    }

    static List<Arguments> testInt() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96, 98, 100));
    }

    @ParameterizedTest
    @MethodSource
    void testLong(final NormalizedNodeStreamVersion version, final int expectedLong, final int expectedLongOne,
            final int expectedLongMax) {
        assertSame(version, 0L, expectedLong);
        assertSame(version, 1L, expectedLongOne);
        assertSame(version, Long.MAX_VALUE, expectedLongMax);
    }

    static List<Arguments> testLong() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96, 100, 104));
    }
}
