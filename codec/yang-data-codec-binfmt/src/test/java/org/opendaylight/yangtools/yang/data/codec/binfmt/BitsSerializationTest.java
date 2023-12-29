/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class BitsSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testEmptyBytes(final NormalizedNodeStreamVersion version, final int size) {
        assertSame(version, ImmutableSet.of(), size);
    }

    static List<Arguments> testEmptyBytes() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96));
    }

    @ParameterizedTest
    @MethodSource
    void testOne(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, ImmutableSet.of("a"), size);
    }

    static List<Arguments> testOne() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 100));
    }

    @ParameterizedTest
    @MethodSource
    void test29(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, fillBits(29), size);
    }

    static List<Arguments> test29() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 226));
    }

    @ParameterizedTest
    @MethodSource
    void test285(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, fillBits(285), size);
    }

    static List<Arguments> test285() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 1_536));
    }

    @ParameterizedTest
    @MethodSource
    void test65821(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, fillBits(65821), size);
    }

    static List<Arguments> test65821() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 456_764));
    }

    @ParameterizedTest
    @MethodSource
    void testTwice65536(final NormalizedNodeStreamVersion version, final int size) {
        final var leaf = ImmutableNodes.leafNode(TestModel.TEST_QNAME, fillBits(65821));

        final var baos = new ByteArrayOutputStream();
        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writeNormalizedNode(leaf);
            nnout.writeNormalizedNode(leaf);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assertions.assertEquals(size, bytes.length);

        try {
            final var input = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assertions.assertEquals(leaf, input.readNormalizedNode());
            Assertions.assertEquals(leaf, input.readNormalizedNode());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
    }

    static List<Arguments> testTwice65536() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 654_045));
    }

    private static ImmutableSet<String> fillBits(final int size) {
        final var builder = ImmutableSet.<String>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add(Integer.toHexString(i));
        }
        final var ret = builder.build();
        Assertions.assertEquals(size, ret.size());
        return ret;
    }
}
