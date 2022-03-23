/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SemVerTest {
    @Test
    public void testSemVer() {
        final SemVer semVer = SemVer.create(5);
        assertNotNull(semVer);

        assertEquals(5, semVer.getMajor());
        assertEquals(0, semVer.getMinor());
        assertEquals(0, semVer.getPatch());

        final SemVer semVer2 = SemVer.valueOf("1.2.3");
        assertNotNull(semVer2);

        assertEquals(1, semVer2.getMajor());
        assertEquals(2, semVer2.getMinor());
        assertEquals(3, semVer2.getPatch());

        final SemVer semVer3 = SemVer.valueOf("1");
        assertNotNull(semVer3);

        assertEquals(1, semVer3.getMajor());
        assertEquals(0, semVer3.getMinor());
        assertEquals(0, semVer3.getPatch());

        final SemVer semVer4 = SemVer.valueOf("1.2");
        assertNotNull(semVer4);

        assertEquals(1, semVer4.getMajor());
        assertEquals(2, semVer4.getMinor());
        assertEquals(0, semVer4.getPatch());

        assertEquals(1, semVer2.compareTo(semVer3));
        assertEquals(-1, semVer3.compareTo(semVer2));
        assertEquals(0, semVer2.compareTo(semVer2));

        assertTrue(semVer2.equals(semVer2));
        assertFalse(semVer2.equals("not equal"));
        assertFalse(semVer2.equals(semVer3));

        assertEquals(semVer2.hashCode(), semVer2.hashCode());

        assertEquals("1.0.0", semVer3.toString());
    }
}
