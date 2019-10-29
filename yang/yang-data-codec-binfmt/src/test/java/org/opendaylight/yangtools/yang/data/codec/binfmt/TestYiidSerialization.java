/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;

@RunWith(Parameterized.class)
public class TestYiidSerialization extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.LITHIUM,
                100, 116, 596, 612, 597, 613, 4196, 4198, 1_052_772, 1_052_776
            },
            new Object[] { NormalizedNodeStreamVersion.NEON_SR2,
                102, 108, 288, 294, 295, 301, 1638, 1646,   394_854,   394_864
            },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1,
                96,  98, 158, 164, 165, 171,  612,   620,   131_684,   131_694
            },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,
                96,  98, 158, 164, 165, 171,  612,   620,   131_684,   131_694
            });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size31;
    @Parameter(5)
    public int uniqueSize31;
    @Parameter(4)
    public int size32;
    @Parameter(6)
    public int uniqueSize32;
    @Parameter(7)
    public int size256;
    @Parameter(8)
    public int uniqueSize256;
    @Parameter(9)
    public int size65792;
    @Parameter(10)
    public int uniqueSize65792;

    @Test
    public void testEmptyIdentifier() {
        assertEquals(YangInstanceIdentifier.empty(), emptySize);
    }

    @Test
    public void testOneIdentifier() {
        assertEquals(YangInstanceIdentifier.of(TestModel.TEST_QNAME), oneSize);
    }

    @Test
    public void testEmptySame() {
        assertSame(YangInstanceIdentifier.empty(), emptySize);
    }

    @Test
    public void test31() {
        assertEquals(fillIdentifier(31), size31);
        assertEquals(fillUniqueIdentifier(31), uniqueSize31);
    }

    @Test
    public void test32() {
        assertEquals(fillIdentifier(32), size32);
        assertEquals(fillUniqueIdentifier(32), uniqueSize32);
    }

    @Test
    public void test256() {
        assertEquals(fillIdentifier(256), size256);
        assertEquals(fillUniqueIdentifier(256), uniqueSize256);
    }

    @Test
    public void test65792() {
        assertEquals(fillIdentifier(65792), size65792);
        assertEquals(fillUniqueIdentifier(65792), uniqueSize65792);
    }

    private static YangInstanceIdentifier fillIdentifier(final int size) {
        final InstanceIdentifierBuilder builder = YangInstanceIdentifier.builder();
        for (int i = 0; i < size; ++i) {
            builder.node(TestModel.TEST_QNAME);
        }
        final YangInstanceIdentifier ret = builder.build();
        Assert.assertEquals(size, ret.getPathArguments().size());
        return ret;
    }

    private static YangInstanceIdentifier fillUniqueIdentifier(final int size) {
        final InstanceIdentifierBuilder builder = YangInstanceIdentifier.builder();
        for (int i = 0; i < size; ++i) {
            builder.node(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(size)));
        }
        final YangInstanceIdentifier ret = builder.build();
        Assert.assertEquals(size, ret.getPathArguments().size());
        return ret;
    }

}
