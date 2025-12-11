/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringEscapingTest {
    @Test
    void testUnescapeNew() {
        //      a\b -----> a\b  (invalid for 7950)
        assertEquals("\\abc", unescape("\\abc", 0));
        assertEquals("abc\\", unescape("abc\\", 3));
        assertEquals("abc\\def", unescape("abc\\def", 3));
        //      a\\b -----> a\b
        assertEquals("\\abc", unescape("\\\\abc", 0));
        assertEquals("abc\\", unescape("abc\\\\", 3));
        assertEquals("abc\\def", unescape("abc\\\\def", 3));
        //      a\\\b -----> a\\b (invalid for 7950)
        assertEquals("\\\\abc", unescape("\\\\\\abc", 0));
        assertEquals("abc\\\\", unescape("abc\\\\\\", 3));
        assertEquals("abc\\\\def", unescape("abc\\\\\\def", 3));
        //      a"b -----> a"b (not passible)
        assertEquals("\"abc", unescape("\"abc", 0));
        assertEquals("abc\"", unescape("abc\"", 3));
        assertEquals("abc\"def", unescape("abc\"def", 3));
        //      a\"b -----> a"b
        assertEquals("\"abc", unescape("\\\"abc", 0));
        assertEquals("abc\"", unescape("abc\\\"", 3));
        assertEquals("abc\"def", unescape("abc\\\"def", 3));
        //      a\\"b -----> a\"b (not passible)
        assertEquals("\\\"abc", unescape("\\\\\"abc", 0));
        assertEquals("abc\\\"", unescape("abc\\\\\"", 3));
        assertEquals("abc\\\"def", unescape("abc\\\\\"def", 3));
        //      a\\\"b -----> a\"b
        assertEquals("\\\"abc", unescape("\\\\\\\"abc", 0));
        assertEquals("abc\\\"", unescape("abc\\\\\\\"", 3));
        assertEquals("abc\\\"def", unescape("abc\\\\\\\"def", 3));
        //      a\tb -----> a   b
        assertEquals("\tabc", unescape("\\tabc", 0));
        assertEquals("abc\t", unescape("abc\\t", 3));
        assertEquals("abc\tdef", unescape("abc\\tdef", 3));
        //      a\\tb -----> a\tb
        assertEquals("\\tabc", unescape("\\\\tabc", 0));
        assertEquals("abc\\t", unescape("abc\\\\t", 3));
        assertEquals("abc\\tdef", unescape("abc\\\\tdef", 3));
        //      a\\\tb -----> a\    b
        assertEquals("\\\tabc", unescape("\\\\\\tabc", 0));
        assertEquals("abc\\\t", unescape("abc\\\\\\t", 3));
        assertEquals("abc\\\tdef", unescape("abc\\\\\\tdef", 3));
        //      a\nb -----> a
        //                  b
        assertEquals("\nabc", unescape("\\nabc", 0));
        assertEquals("abc\n", unescape("abc\\n", 3));
        assertEquals("abc\ndef", unescape("abc\\ndef", 3));
        //      a\\nb -----> a\nb
        assertEquals("\\nabc", unescape("\\\\nabc", 0));
        assertEquals("abc\\n", unescape("abc\\\\n", 3));
        assertEquals("abc\\ndef", unescape("abc\\\\ndef", 3));
        //      a\\\nb -----> a\
        //                    b
        assertEquals("\\\nabc", unescape("\\\\\\nabc", 0));
        assertEquals("abc\\\n", unescape("abc\\\\\\n", 3));
        assertEquals("abc\\\ndef", unescape("abc\\\\\\ndef", 3));

        assertEquals("\\\nabc abc\\n\nabc abc\t", unescape("\\\\\\nabc abc\\\\n\\nabc abc\\t", 0));
    }

    private static String unescape(final String str, final int backslash) {
        final var sb = new StringBuilder(str.length());
        StringEscaping.unescapeBackslash(sb, str, backslash);
        return sb.toString();
    }
}
