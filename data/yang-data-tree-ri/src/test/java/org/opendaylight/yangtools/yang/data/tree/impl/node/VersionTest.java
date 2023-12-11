/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VersionTest {
    @ParameterizedTest
    void testInitial(final boolean tracking) {
        final var v1 = Version.initial(tracking);
        final var v2 = Version.initial(tracking);

        assertNotEquals(v1, v2);
        assertNotEquals(v2, v1);
    }

    @ParameterizedTest
    @MethodSource("versionTypes")
    void testNext(final boolean tracking) {
        final var v1 = Version.initial(tracking);
        final var v2 = v1.next();
        final var v3 = v2.next();
        final var v4 = v1.next();

        assertNotEquals(v3, v4);
        assertNotEquals(v4, v3);
    }

    private static List<Arguments> versionTypes() {
        return List.of(Arguments.of(false), Arguments.of(true));
    }
}
