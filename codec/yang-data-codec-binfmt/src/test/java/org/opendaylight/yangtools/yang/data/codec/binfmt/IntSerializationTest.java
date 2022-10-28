/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IntSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.NEON_SR2,   99, 99, 100, 100, 102, 102, 102, 106, 106, 106 },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1, 96, 97, 96,   98,  96,  98, 100,  96, 100, 104 },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,  96, 97, 96,   98,  96,  98, 100,  96, 100, 104 });
    }

    @Parameter(1)
    public int expectedByte;
    @Parameter(2)
    public int expectedByteOne;
    @Parameter(3)
    public int expectedShort;
    @Parameter(4)
    public int expectedShortOne;
    @Parameter(5)
    public int expectedInt;
    @Parameter(6)
    public int expectedIntOne;
    @Parameter(7)
    public int expectedIntMax;
    @Parameter(8)
    public int expectedLong;
    @Parameter(9)
    public int expectedLongOne;
    @Parameter(10)
    public int expectedLongMax;

    @Test
    public void testByte() {
        assertSame((byte) 0, expectedByte);
        assertSame((byte) 1, expectedByteOne);
        assertSame(Byte.MAX_VALUE, expectedByteOne);
    }

    @Test
    public void testShort() {
        assertSame((short) 0, expectedShort);
        assertSame((short) 1, expectedShortOne);
        assertSame(Short.MAX_VALUE, expectedShortOne);
    }

    @Test
    public void testInt() {
        assertSame(0, expectedInt);
        assertSame(1, expectedIntOne);
        assertSame(Integer.MAX_VALUE, expectedIntMax);
    }

    @Test
    public void testLong() {
        assertSame(0L, expectedLong);
        assertSame(1L, expectedLongOne);
        assertSame(Long.MAX_VALUE, expectedLongMax);
    }
}
