/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.util.regex.XSDRegex.NAME_CHAR;
import static org.opendaylight.yangtools.yang.model.util.regex.XSDRegex.NAME_START_CHAR;

import java.util.regex.Pattern;
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
        assertJavaPattern("^.$", ".");
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

        assertJavaPattern("^[a-b]$", "[a-b]");
        assertJavaPattern("^[^a-b]$", "[^a-b]");

        assertJavaPattern("^[\n-\r]$", "[\n-\r]");
        assertJavaPattern("^[^\n-\r]$", "[^\n-\r]");

        assertJavaPattern("^[.]$", "[.]");
        assertJavaPattern("^[^.]$", "[^.]");

        assertJavaPattern("^[" + NAME_CHAR + "]$", "\\c");
        assertJavaPattern("^[^" + NAME_CHAR + "]$", "\\C");
        assertJavaPattern("^\\p{Nd}$", "\\d");
        assertJavaPattern("^\\P{Nd}$", "\\D");
        assertJavaPattern("^[" + NAME_START_CHAR + "]$", "\\i");
        assertJavaPattern("^[^" + NAME_START_CHAR + "]$", "\\I");
        assertJavaPattern("^[ \\t\\n\\r]$", "\\s");
        assertJavaPattern("^[^ \\t\\n\\r]$", "\\S");
        assertJavaPattern("^[\\P{P}&&\\P{Z}&&\\P{C}]$", "\\w");
        assertJavaPattern("^[\\p{P}\\p{Z}\\p{C}]$", "\\W");
        assertJavaPattern("^\\p{Nd}$", "\\p{Nd}");
        assertJavaPattern("^\\p{InBasicLatin}$", "\\p{IsBasicLatin}");
        assertJavaPattern("^\\P{InBasicLatin}$", "\\P{IsBasicLatin}");
        assertJavaPattern("^[\\p{InBasicLatin}]$", "[\\p{IsBasicLatin}]");
        assertJavaPattern("^[\\P{InBasicLatin}]$", "[\\P{IsBasicLatin}]");
    }

    @Test
    public void testDash() {
        assertJavaPattern("^-$", "-");
        assertJavaPattern("^[-]$", "[-]");
        assertJavaPattern("^[a-]$", "[a-]");
        assertJavaPattern("^[-a]$", "[-a]");
        assertJavaPattern("^[a&&[^b]]$", "[a-[b]]");
        assertJavaPattern("^[a&&[b]]$", "[a-[^b]]");
        assertJavaPattern("^[\\-&&[^b]]$", "[--[b]]");
        assertJavaPattern("^[a\\-&&[^b]]$", "[a--[b]]");
    }

    @Test
    public void testRegEx() {
        assertJavaPattern("^(a|b)?$", "(a|b)?");
    }

    @Test
    public void testAmpersandEscape() {
        assertJavaPattern("^&$", "&");
        assertJavaPattern("^&&$", "&&");
        assertJavaPattern("^[&]$", "[&]");
        assertJavaPattern("^[&]$", "[&&]");
        assertJavaPattern("^[a&b]$", "[a&&b]");
        assertJavaPattern("^[a&b]$", "[a&&b&&]");
    }

    @Test
    public void testCaret() {
        assertJavaPattern("^\\^$", "^");
    }

    @Test
    public void testDollarEscape() {
        assertJavaPattern("^[$]$", "[$]");
        assertJavaPattern("^\\$$", "$");
    }

    @Test
    public void testMatch() {
        assertMatch("a", "a");
        assertNotMatch("[a]", "b");
        assertMatch("[a]", "a");
        assertNotMatch("[a]", "b");
        assertMatch("[ab-[b]]", "a");
        assertNotMatch("[ab-[b]]", "b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCharClass() {
        XSDRegex.parse("[^]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalUnicodeBlockClass() {
        compileRegex("\\P{IsSomethingImpossible}");
    }

    private static void assertJavaPattern(final String expected, final String xsd) {
        assertEquals(expected, compileRegex(xsd).toString());
    }

    private static void assertMatch(final String xsd, final String data) {
        assertTrue(compileRegex(xsd).matcher(data).matches());
    }

    private static void assertNotMatch(final String xsd, final String data) {
        assertFalse(compileRegex(xsd).matcher(data).matches());
    }

    private static Pattern compileRegex(final String xsd) {
        final XSDRegex regex = XSDRegex.parse(xsd);
        assertEquals(xsd, regex.toString());
        return regex.toJavaPattern();
    }
}
