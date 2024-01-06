/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class YangVersionTest {
    @Test
    public void testGetReference() {
        assertEquals("RFC6020", YangVersion.VERSION_1.reference());
        assertEquals("RFC7950", YangVersion.VERSION_1_1.reference());
    }

    @Test
    public void testParse() {
        assertNull(YangVersion.forString(""));
        assertNull(YangVersion.forString("1."));
        assertEquals(YangVersion.VERSION_1, YangVersion.forString("1"));
        assertEquals(YangVersion.VERSION_1_1, YangVersion.forString("1.1"));
    }

    @Test
    public void testParseNull() {
        assertThrows(NullPointerException.class, () -> YangVersion.forString(null));
    }

    @Test
    public void testToString() {
        assertEquals("1", YangVersion.VERSION_1.toString());
        assertEquals("1.1", YangVersion.VERSION_1_1.toString());
    }
}
