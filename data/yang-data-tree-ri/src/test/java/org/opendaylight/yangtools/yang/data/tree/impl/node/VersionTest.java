/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void testInitial() {
        final var v1 = Version.initial();
        final var v2 = Version.initial();

        assertNotEquals(v1, v2);
        assertNotEquals(v2, v1);
    }

    @Test
    void testNext() {
        final var v1 = Version.initial();
        final var v2 = v1.next();
        final var v3 = v2.next();
        final var v4 = v1.next();

        assertNotEquals(v3, v4);
        assertNotEquals(v4, v3);
    }
}
