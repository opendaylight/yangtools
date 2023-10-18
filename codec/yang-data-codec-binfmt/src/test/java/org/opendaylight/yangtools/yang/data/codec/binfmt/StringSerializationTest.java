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

class StringSerializationTest extends AbstractSerializationTest {
    private static final String STR_MEDIUM = "a".repeat(32767);
    private static final String STR_HUGE = "©".repeat(16777216);

    @ParameterizedTest
    @MethodSource
    void testEmptySame(final NormalizedNodeStreamVersion version, final int size) {
        assertSame(version, "", size);
    }

    static List<Arguments> testEmptySame() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96));
    }

    @ParameterizedTest
    @MethodSource
    public void testOne(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, "a", size);
    }

    static List<Arguments> testOne() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 99));
    }

    @ParameterizedTest
    @MethodSource
    void testMedium(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, STR_MEDIUM, size);
    }

    static List<Arguments> testMedium() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 32_865));
    }

    @ParameterizedTest
    @MethodSource
    void testHuge(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, STR_HUGE, size);
    }

    static List<Arguments> testHuge() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 33_554_532));
    }
}
