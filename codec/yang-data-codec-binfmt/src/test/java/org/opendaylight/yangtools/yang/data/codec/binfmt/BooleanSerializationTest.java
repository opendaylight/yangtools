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

class BooleanSerializationTest extends AbstractSerializationTest {
    @ParameterizedTest
    @MethodSource("parameters")
    void testTrue(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, Boolean.TRUE, size);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testFalse(final NormalizedNodeStreamVersion version, final int size) {
        assertEquals(version, Boolean.FALSE, size);
    }

    private static List<Arguments> parameters() {
        return List.of(Arguments.of(NormalizedNodeStreamVersion.POTASSIUM, 96));
    }
}
