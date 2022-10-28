/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

@RunWith(Parameterized.class)
public class NipSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.NEON_SR2,   101, 116, 176, 4181, 1_180_245, 1_772_383 },
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1,  95, 107, 156, 3409,   982_867, 1_443_164 },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,   95, 107, 156, 3409,   982_867, 1_443_164 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size5;
    @Parameter(4)
    public int size256;
    @Parameter(5)
    public int size65792;
    @Parameter(6)
    public int twiceSize65792;

    @Test
    public void testEmptyIdentifier() {
        assertEquals(createIdentifier(0), emptySize);
    }

    @Test
    public void testOneIdentifier() {
        assertEquals(createIdentifier(1), oneSize);
    }

    @Test
    public void test5() {
        assertEquals(createIdentifier(5), size5);
    }

    @Test
    public void test256() {
        assertEquals(createIdentifier(256), size256);
    }

    @Test
    public void test65536() {
        assertEquals(createIdentifier(65792), size65792);
    }

    @Test
    public void testTwice65792() {
        final NodeIdentifierWithPredicates nip = createIdentifier(65792);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NormalizedNodeDataOutput nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writePathArgument(nip);
            nnout.writePathArgument(nip);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals(twiceSize65792, bytes.length);

        try {
            final NormalizedNodeDataInput input = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assert.assertEquals(nip, input.readPathArgument());
            Assert.assertEquals(nip, input.readPathArgument());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
    }

    private static NodeIdentifierWithPredicates createIdentifier(final int size) {
        final Map<QName, Object> predicates = Maps.newHashMapWithExpectedSize(size);
        for (QName qname : generateQNames(size)) {
            predicates.put(qname, "a");
        }
        return NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME, predicates);
    }
}
