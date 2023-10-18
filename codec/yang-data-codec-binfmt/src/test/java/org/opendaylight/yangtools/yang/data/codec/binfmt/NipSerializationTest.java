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
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

class NipSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testEmptyIdentifier(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(0), size);
    }

    static List<Arguments> testEmptyIdentifier() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 95));
    }

    @ParameterizedTest
    @MethodSource
    void testOneIdentifier(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(1), size);
    }

    static List<Arguments> testOneIdentifier() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 107));
    }

    @ParameterizedTest
    @MethodSource
    void test5(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(5), size);
    }

    static List<Arguments> test5() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 155));
    }

    @ParameterizedTest
    @MethodSource
    void test13(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(13), size);
    }

    static List<Arguments> test13() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 252));
    }

    @ParameterizedTest
    @MethodSource
    void test256(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(256), size);
    }


    static List<Arguments> test256() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 3_409));
    }

    @ParameterizedTest
    @MethodSource
    void test65792(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, createIdentifier(65792), size);
    }

    static List<Arguments> test65792() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 982_867));
    }

    @ParameterizedTest
    @MethodSource
    void testTwice65792(final NormalizedNodeStreamVersion version, final int size) {
        final var nip = createIdentifier(65792);

        final var baos = new ByteArrayOutputStream();
        try (var nnout = version.newDataOutput(ByteStreams.newDataOutput(baos))) {
            nnout.writePathArgument(nip);
            nnout.writePathArgument(nip);
        } catch (IOException e) {
            throw new AssertionError("Failed to serialize", e);
        }

        final byte[] bytes = baos.toByteArray();
        Assertions.assertEquals(size, bytes.length);

        try {
            final var input = NormalizedNodeDataInput.newDataInput(ByteStreams.newDataInput(bytes));
            Assertions.assertEquals(nip, input.readPathArgument());
            Assertions.assertEquals(nip, input.readPathArgument());
        } catch (IOException e) {
            throw new AssertionError("Failed to deserialize", e);
        }
    }

    static List<Arguments> testTwice65792() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 1_443_164));
    }

    private static NodeIdentifierWithPredicates createIdentifier(final int size) {
        final var predicates = Maps.<QName, Object>newHashMapWithExpectedSize(size);
        for (var qname : generateQNames(size)) {
            predicates.put(qname, "a");
        }
        return NodeIdentifierWithPredicates.of(TestModel.TEST_QNAME, predicates);
    }
}
