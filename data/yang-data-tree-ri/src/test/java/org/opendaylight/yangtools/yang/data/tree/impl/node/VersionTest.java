/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testInitial() {
        final Version v1 = Version.initial();
        final Version v2 = Version.initial();

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    }

    @Test
    public void testNext() {
        final Version v1 = Version.initial();
        final Version v2 = v1.next();
        final Version v3 = v2.next();
        final Version v4 = v1.next();

        assertFalse(v3.equals(v4));
        assertFalse(v4.equals(v3));
    }
}
