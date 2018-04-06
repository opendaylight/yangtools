/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DottedQuadStringTest {
    @Test
    public void testValueOfInt() {
        assertEquals("0.0.0.0", createString(0));
        assertEquals("0.0.0.1", createString(1));
        assertEquals("0.0.0.16", createString(0x10));
        assertEquals("0.0.1.0", createString(0x100));
        assertEquals("0.0.16.0", createString(0x1000));
        assertEquals("0.1.0.0", createString(0x10000));
        assertEquals("0.16.0.0", createString(0x100000));
        assertEquals("1.0.0.0", createString(0x1000000));
        assertEquals("16.0.0.0", createString(0x10000000));
        assertEquals("127.255.255.255", createString(Integer.MAX_VALUE));
        assertEquals("128.0.0.0", createString(Integer.MIN_VALUE));

        checkString("0.0.0.0");
        checkString("0.0.0.1");
        checkString("0.0.0.16");
        checkString("0.0.1.0");
        checkString("0.0.16.0");
        checkString("0.1.0.0");
        checkString("0.16.0.0");
        checkString("1.0.0.0");
        checkString("16.0.0.0");
        checkString("127.255.255.255");
        checkString("128.0.0.0");
    }

    private static String createString(final int input) {
        final DottedQuadString quad = DottedQuadString.valueOf(input);
        assertEquals(input, quad.toIntBits());
        assertEquals(4, quad.getLength());
        assertEquals(input, quad.hashCode());
        return quad.toCanonicalString();
    }

    private static void checkString(final String input) {
        assertEquals(input, DottedQuadStringSupport.getInstance().fromString(input).toString());
    }
}
