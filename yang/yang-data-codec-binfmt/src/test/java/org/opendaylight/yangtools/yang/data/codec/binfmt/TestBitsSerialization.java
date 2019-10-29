/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestBitsSerialization extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.LITHIUM,    100, 104, 229, 1538, 456764 },
            new Object[] { NormalizedNodeStreamVersion.NEON_SR2,   102, 106, 231, 1540, 456766 },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1,  96, 100, 226, 1536, 456764 },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,   96, 100, 226, 1536, 456764 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size29;
    @Parameter(4)
    public int size285;
    @Parameter(5)
    public int size65821;

    @Test
    public void testEmptyBytes() {
        assertEquals(ImmutableSet.of(), emptySize);
    }

    @Test
    public void testOne() {
        assertEquals(ImmutableSet.of("a"), oneSize);
    }

    @Test
    public void test29() {
        assertEquals(fillBits(29), size29);
    }

    @Test
    public void test285() {
        assertEquals(fillBits(285), size285);
    }

    @Test
    public void test65821() {
        assertEquals(fillBits(65821), size65821);
    }

    @Test
    public void testEmptySame() {
        assertSame(ImmutableSet.of(), emptySize);
    }

    private static ImmutableSet<String> fillBits(final int size) {
        final Builder<String> builder = ImmutableSet.builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add(Integer.toHexString(i));
        }
        final ImmutableSet<String> ret = builder.build();
        Assert.assertEquals(size, ret.size());
        return ret;
    }
}
