/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BytesSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource
    void testEmptyBytes(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, new byte[0], size);
    }

    static List<Arguments> testEmptyBytes() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96));
    }

    @ParameterizedTest
    @MethodSource
    void testOne(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, randomBytes(1), size);
    }

    static List<Arguments> testOne() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 97));
    }

    @ParameterizedTest
    @MethodSource
    void test128(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, randomBytes(128), size);
    }

    static List<Arguments> test128() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 225));
    }

    @ParameterizedTest
    @MethodSource
    void test384(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, randomBytes(384), size);
    }

    static List<Arguments> test384() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 482));
    }

    @ParameterizedTest
    @MethodSource
    void test65920(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, randomBytes(65920), size);
    }

    static List<Arguments> test65920() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 66_020));
    }

    private static byte[] randomBytes(final int size) {
        final byte[] ret = new byte[size];
        ThreadLocalRandom.current().nextBytes(ret);
        return ret;
    }
}
