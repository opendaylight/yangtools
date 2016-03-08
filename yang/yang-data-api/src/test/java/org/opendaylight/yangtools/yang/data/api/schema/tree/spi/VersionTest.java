/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testInitial() {
        final Version v1 = Version.initial();
        final Version v2 = Version.initial();

        assertNotEquals(v1, v2);
        assertNotEquals(v2, v1);
     }

    @Test
    public void testNext() {
        final Version v1 = Version.initial();
        final Version v2 = v1.next();
        final Version v3 = v2.next();
        final Version v4 = v1.next();

        assertNotEquals(v3, v4);
        assertNotEquals(v4, v3);
    }
}
