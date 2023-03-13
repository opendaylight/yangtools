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
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class YiidSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testOneIdentifier(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, YangInstanceIdentifier.of(TestModel.TEST_QNAME), size);
    }

    private static List<Arguments> testOneIdentifier() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 98));
    }

    @ParameterizedTest
    @MethodSource
    void test31(final NormalizedNodeStreamVersion version, final int size, final int uniqueSize) {
        assertEquals(version, fillIdentifier(31), size);
        assertEquals(version, fillUniqueIdentifier(31), uniqueSize);
    }

    private static List<Arguments> test31() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 158, 359));
    }

    @ParameterizedTest
    @MethodSource
    void test32(final NormalizedNodeStreamVersion version, final int size, final int uniqueSize) {
        assertEquals(version, fillIdentifier(32), size);
        assertEquals(version, fillUniqueIdentifier(32), uniqueSize);
    }

    private static List<Arguments> test32() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 164, 372));
    }

    @ParameterizedTest
    @MethodSource
    void test256(final NormalizedNodeStreamVersion version, final int size, final int uniqueSize) {
        assertEquals(version, fillIdentifier(256), size);
        assertEquals(version, fillUniqueIdentifier(256), uniqueSize);
    }

    private static List<Arguments> test256() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 612, 2_388));
    }

    @ParameterizedTest
    @MethodSource
    void test65792(final NormalizedNodeStreamVersion version, final int size, final int uniqueSize) {
        assertEquals(version, fillIdentifier(65792), size);
        assertEquals(version, fillUniqueIdentifier(65792), uniqueSize);
    }

    private static List<Arguments> test65792() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 131_684, 719_700));
    }

    @ParameterizedTest
    @MethodSource
    void testTwice65536(final NormalizedNodeStreamVersion version, final int size) {
        final var yiid = fillUniqueIdentifier(65792);

        final var baos = new ByteArrayOutputStream();
        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writeYangInstanceIdentifier(yiid);
            nnout.writeYangInstanceIdentifier(yiid);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assertions.assertEquals(size, bytes.length);

        try {
            final var input = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assertions.assertEquals(yiid, input.readYangInstanceIdentifier());
            Assertions.assertEquals(yiid, input.readYangInstanceIdentifier());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
    }

    private static List<Arguments> testTwice65536() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 916_815));
    }

    private static YangInstanceIdentifier fillIdentifier(final int size) {
        final var builder = YangInstanceIdentifier.builder();
        for (int i = 0; i < size; ++i) {
            builder.node(TestModel.TEST_QNAME);
        }
        final var ret = builder.build();
        Assertions.assertEquals(size, ret.getPathArguments().size());
        return ret;
    }

    private static YangInstanceIdentifier fillUniqueIdentifier(final int size) {
        final var builder = YangInstanceIdentifier.builder();
        for (int i = 0; i < size; ++i) {
            builder.node(QName.create(TestModel.TEST_QNAME, "a" + Integer.toHexString(i)));
        }
        final var ret = builder.build();
        Assertions.assertEquals(size, ret.getPathArguments().size());
        return ret;
    }
}
