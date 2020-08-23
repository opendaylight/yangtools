/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.parser.rfc7950.ir.AntlrSupport.trimWhitespace;

import org.junit.Test;

public class AntlrSupportTest {
    @Test
    public void testTrimWhitespace() {
        assertEquals("\n", trimWhitespace("\n", 0));
        assertEquals("\n", trimWhitespace("\n", 5));
        assertEquals("\n\n\n\n", trimWhitespace("\n\n\n\n", 0));
        assertEquals("\n\n\n\n", trimWhitespace("\n\n\n\n", 5));
        assertEquals("abc\n\n", trimWhitespace("abc \n  \n", 0));
        assertEquals("abc\n\n", trimWhitespace("abc \n  \n", 1));
        assertEquals("abc\n  ", trimWhitespace("abc\n   ", 0));
        assertEquals("abc\n", trimWhitespace("abc\n   ", 2));
        assertEquals("abc\n\n", trimWhitespace("abc\n   \n", 2));
        assertEquals("abc\n        ", trimWhitespace("abc\n\t ", 0));
        assertEquals("abc\n      ", trimWhitespace("abc\n\t ", 2));
        assertEquals("abc\n    ", trimWhitespace("abc\n\t ", 4));
        assertEquals("abc\n    ", trimWhitespace("abc\n \t", 4));
        assertEquals("abc\n   a\n    a\n", trimWhitespace("abc\n\ta\n\t a\n", 4));
        assertEquals("abc\n\n    a\n", trimWhitespace("abc\n\t\n\t a\n", 4));
        assertEquals("   \ta\n", trimWhitespace("   \ta\n", 3));
        assertEquals("   \ta\n", trimWhitespace("   \ta\n  ", 3));
        assertEquals("   \ta\n", trimWhitespace("   \ta\n   ", 3));
        assertEquals("   \ta\n", trimWhitespace("   \ta\n    ", 3));
        assertEquals("   \ta\n ", trimWhitespace("   \ta\n     ", 3));
    }
}
