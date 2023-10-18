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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

class UintSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource("versions")
    void testUint8(final NormalizedNodeStreamVersion version) {
        assertSame(version, Uint8.ZERO, 96);
        assertSame(version, Uint8.ONE, 97);
    }

    @ParameterizedTest
    @MethodSource("versions")
    void testUint16(final NormalizedNodeStreamVersion version) {
        assertSame(version, Uint16.ZERO, 96);
        assertSame(version, Uint16.ONE, 98);
    }

    @ParameterizedTest
    @MethodSource("versions")
    void testUint32(final NormalizedNodeStreamVersion version) {
        assertSame(version, Uint32.ZERO, 96);
        assertSame(version, Uint32.ONE, 98);
        assertEquals(version, Uint32.MAX_VALUE, 100);
    }

    @ParameterizedTest
    @MethodSource("versions")
    void testUint64(final NormalizedNodeStreamVersion version) {
        assertSame(version, Uint64.ZERO, 96);
        assertSame(version, Uint64.ONE, 100);
        assertEquals(version, Uint64.MAX_VALUE, 104);
    }

    static List<Arguments> versions() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM));
    }
}
