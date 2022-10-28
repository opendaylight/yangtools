/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class YiidSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1,
                96,   98, 158, 359, 164, 372, 612, 2388, 131_684, 719_700, 916_815
            },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,
                96,   98, 158, 359, 164, 372, 612, 2388, 131_684, 719_700, 916_815
            });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size31;
    @Parameter(4)
    public int uniqueSize31;
    @Parameter(5)
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
    @Parameter(11)
    public int twiceSize65792;

    @Test
    public void testEmptyIdentifier() {
        assertSame(YangInstanceIdentifier.empty(), emptySize);
    }

    @Test
    public void testOneIdentifier() {
        assertEquals(YangInstanceIdentifier.of(TestModel.TEST_QNAME), oneSize);
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

    @Test
    public void testTwice65536() {
        final YangInstanceIdentifier yiid = fillUniqueIdentifier(65792);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writeYangInstanceIdentifier(yiid);
            nnout.writeYangInstanceIdentifier(yiid);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(twiceSize65792, bytes.length);

        try {
            final NormalizedNodeDataInput input = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assert.assertEquals(yiid, input.readYangInstanceIdentifier());
            Assert.assertEquals(yiid, input.readYangInstanceIdentifier());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
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
            builder.node(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(i)));
        }
        final YangInstanceIdentifier ret = builder.build();
        Assert.assertEquals(size, ret.getPathArguments().size());
        return ret;
    }
}
