/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class Bug5410Test {
    @Test
    public void testCaret() {
        testPattern("^", "\\^");
    }

    @Test
    public void testTextCaret() {
        testPattern("abc^", "abc\\^");
    }

    @Test
    public void testTextDollar() {
        testPattern("abc$", "abc\\$");
    }

    @Test
    public void testCaretCaret() {
        testPattern("^^", "\\^\\^");
    }

    @Test
    public void testCaretDollar() {
        testPattern("^$", "\\^\\$");
    }

    @Test
    public void testDot() {
        testPattern(".", ".");
    }

    @Test
    public void testNotColon() {
        testPattern("[^:]+", "[^:]+");
    }

    @Test
    public void testDollar() {
        testPattern("$", "\\$");
    }

    @Test
    public void testDollarOneDollar() {
        testPattern("$1$", "\\$1\\$");
    }

    @Test
    public void testDollarPercentRange() {
        testPattern("[$-%]+", "[$-%]+");
    }

    @Test
    public void testDollarRange() {
        testPattern("[$$]+", "[$$]+");
    }

    @Test
    public void testDollarCaretRange() {
        testPattern("[$^]+", "[$^]+");
    }

    @Test
    public void testSimple() {
        testPattern("abc", "abc");
    }

    @Test
    public void testDotPlus() {
        testPattern(".+", ".+");
    }

    @Test
    public void testDotStar() {
        testPattern(".*", ".*");
    }

    @Test
    public void testSimpleOptional() {
        testPattern("a?", "a?");
    }

    @Test
    public void testRangeOptional() {
        testPattern("[a-z]?", "[a-z]?");
    }

    @Test
    public void testInvalidXSDRegexes() throws UnsupportedEncodingException {
        testInvalidPattern("$^a^[$^\\]", "Unclosed character class");
        testInvalidPattern("$(\\)", "Unclosed group");
    }

    @Test
    public void testJavaPattern() {
        testPattern("^[$^]+$", ImmutableList.of("$^", "^", "$"), ImmutableList.of("\\", "a"));
        testPattern("^[^$-^]$", ImmutableList.of("a", "_", "#"), ImmutableList.of("%", "^", "$", "]", "\\"));
    }

    @Test
    public void testJavaRegexFromXSD() {
        testPattern("^[^:]+$", "^(?:\\^[^:]+\\$)$", ImmutableList.of("^a$", "^abc$"),
                ImmutableList.of("abc$", "^abc", "^a:bc$"));
        testPattern("^[$^]$", "^(?:\\^[$^]\\$)$", ImmutableList.of("^^$", "^$$"),
            ImmutableList.of("^^", "^$", "$^", "$$"));
        testPattern("[$-%]+", "^(?:[$-%]+)$", ImmutableList.of("$", "%", "%$"),
            ImmutableList.of("$-", "$-%", "-", "^"));
        testPattern("[$-&]+", "^(?:[$-&]+)$", ImmutableList.of("$", "%&", "%$", "$%&"),
            ImmutableList.of("#", "$-&", "'"));

        testPattern("[a-z&&[^m-p]]+", "^(?:[a-z&&[^m-p]]+)$", ImmutableList.of("a", "z", "az"),
                ImmutableList.of("m", "anz", "o"));
        testPattern("^[\\[-b&&[^^-a]]+$", "^(?:\\^[\\[-b&&[^^-a]]+\\$)$", ImmutableList.of("^[$", "^\\$", "^]$", "^b$"),
                ImmutableList.of("^a$", "^^$", "^_$"));

        // FIXME: YANGTOOLS-887: these patterns are not translated correctly, "&&" is a different construct in XSD
        //        testPattern("[^^-~&&[^$-^]]", "^(?:[^^-~&&[^$-^]])$", ImmutableList.of("!", "\"", "#"),
        //                ImmutableList.of("a", "A", "z", "Z", "$", "%", "^", "}"));
        //        testPattern("\\\\\\[^[^^-~&&[^$-^]]", "^(?:\\\\\\[\\^[^^-~&&[^$-^]])$",
        //                ImmutableList.of("\\[^ ", "\\[^!", "\\[^\"", "\\[^#"),
        //                ImmutableList.of("\\[^a", "\\[^A", "\\[^z", "\\[^Z", "\\[^$", "\\[^%", "\\[^^", "\\[^}"));
        //        testPattern("^\\[^\\\\[^^-b&&[^\\[-\\]]]\\]^", "^(?:\\^\\[\\^\\\\[^^-b&&[^\\[-\\]]]\\]\\^)$",
        //                ImmutableList.of("^[^\\c]^", "^[^\\Z]^"),
        //                ImmutableList.of("^[^\\[]^", "^[^\\\\]^", "^[^\\]]^", "^[^\\^]^", "^[^\\_]^", "^[^\\b]^"));
        //        testPattern("[\\^]$", "^(?:[\\^]\\$)$", ImmutableList.of("^$"),
        //                ImmutableList.of("^", "$", "$^", "\\", "\\^", "\\^\\", "\\^\\$"));
    }

    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    private static void testInvalidPattern(final String xsdRegex, final String expectedMessage)
            throws UnsupportedEncodingException {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        RegexUtils.getJavaRegexFromXSD(xsdRegex);

        final String testLog = output.toString();
        assertTrue(testLog.contains(expectedMessage));
        System.setOut(stdout);
    }

    private static boolean testMatch(final String javaRegex, final String value) {
        return value.matches(javaRegex);
    }

    private static void testPattern(final String xsdRegex, final String unanchoredJavaRegex) {
        testPattern(xsdRegex, "^(?:" + unanchoredJavaRegex + ")$", ImmutableList.of(), ImmutableList.of());
    }

    private static void testPattern(final String javaRegex, final List<String> positiveMatches,
            final List<String> negativeMatches) {
        for (final String value : positiveMatches) {
            assertTrue("Value '" + value + "' does not match java regex '" + javaRegex + "'",
                    testMatch(javaRegex, value));
        }
        for (final String value : negativeMatches) {
            assertFalse("Value '" + value + "' matches java regex '" + javaRegex + "'", testMatch(javaRegex, value));
        }
    }

    private static void testPattern(final String xsdRegex, final String expectedJavaRegex,
            final List<String> positiveMatches, final List<String> negativeMatches) {
        final String javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD(xsdRegex);
        assertEquals(expectedJavaRegex, javaRegexFromXSD);

        for (final String value : positiveMatches) {
            assertTrue("Value '" + value + "' does not match java regex '" + javaRegexFromXSD + "'",
                    testMatch(javaRegexFromXSD, value));
        }
        for (final String value : negativeMatches) {
            assertFalse("Value '" + value + "' matches java regex '" + javaRegexFromXSD + "'",
                    testMatch(javaRegexFromXSD, value));
        }
    }
}
