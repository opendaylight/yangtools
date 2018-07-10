/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class XSDRegexTest {
    @Test
    public void testEmpty() {
        assertJavaPattern("^$", "");
    }

    @Test
    public void testSingleEscape() {
        assertJavaPattern("^\\n$", "\\n");
        assertJavaPattern("^\\r$", "\\r");
        assertJavaPattern("^\\t$", "\\t");
        assertJavaPattern("^\\\\$", "\\\\");
        assertJavaPattern("^\\|$", "\\|");
        assertJavaPattern("^\\.$", "\\.");
        assertJavaPattern("^\\^$", "\\^");
        assertJavaPattern("^\\?$", "\\?");
        assertJavaPattern("^\\*$", "\\*");
        assertJavaPattern("^\\+$", "\\+");
        assertJavaPattern("^\\{$", "\\{");
        assertJavaPattern("^\\}$", "\\}");
        assertJavaPattern("^\\($", "\\(");
        assertJavaPattern("^\\)$", "\\)");
        assertJavaPattern("^\\[$", "\\[");
        assertJavaPattern("^\\]$", "\\]");
    }

    @Test
    public void testSimple() {
        assertJavaPattern("^a$", "a");
        assertJavaPattern("^6$", "6");
    }

    @Test
    public void testSimpleAlternatives() {
        assertJavaPattern("^(?:a|b)$", "a|b");
        assertJavaPattern("^(?:a|b|c)$", "a|b|c");
    }

    @Test
    public void testQuantified() {
        assertJavaPattern("^a{1}$", "a{1}");
        assertJavaPattern("^a{1,}$", "a{1,}");
        assertJavaPattern("^a{1,2}$", "a{1,2}");
        assertJavaPattern("^a?$", "a?");
        assertJavaPattern("^a+$", "a+");
        assertJavaPattern("^a*$", "a*");
    }

    @Test
    public void testCharClass() {
        assertJavaPattern("^[ab]$", "[ab]");
        assertJavaPattern("^[^ab]$", "[^ab]");
        assertJavaPattern("^\\p{Nd}$", "\\d");
        assertJavaPattern("^\\P{Nd}$", "\\D");
        assertJavaPattern("^[ \\t\\n\\r]$", "\\s");
        assertJavaPattern("^[^ \\t\\n\\r]$", "\\S");
        assertJavaPattern("^[\\P{P}&&\\P{Z}&&\\P{C}]$", "\\w");
        assertJavaPattern("^[\\p{P}\\p{Z}\\p{C}]$", "\\W");
        assertJavaPattern("^\\p{Nd}$", "\\p{Nd}");
        assertJavaPattern("^\\p{InBasicLatin}$", "\\p{IsBasicLatin}");
        assertJavaPattern("^\\P{InBasicLatin}$", "\\P{IsBasicLatin}");
    }

    @Test
    public void testRegEx() {
        assertJavaPattern("^(a|b)?$", "(a|b)?");
    }

    @Ignore
    @Test
    public void testAmpersandEscape() {
        assertJavaPattern("^&$", "&");
        assertJavaPattern("^&&$", "&&");
        assertJavaPattern("^[&]$", "[&]");
        assertJavaPattern("^[\\&]$", "[&&]");
        assertJavaPattern("^[a\\&b]$", "[a&&b]");
    }

    @Ignore
    @Test
    public void testDollarEscape() {
        assertJavaPattern("^[$]$", "[$]");
        assertJavaPattern("^\\$$", "$");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCharClass() {
        XSDRegex.parse("[^]");
    }

    private static void assertJavaPattern(final String expected, final String xsd) {
        final XSDRegex regex = XSDRegex.parse(xsd);
        assertEquals(xsd, regex.toString());
        assertEquals(expected, regex.toJavaPattern().toString());
    }
}
