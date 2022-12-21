/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class Bug5410Test {
    @Test
    void testCaret() {
        testPattern("^", "\\^");
    }

    @Test
    void testTextCaret() {
        testPattern("abc^", "abc\\^");
    }

    @Test
    void testTextDollar() {
        testPattern("abc$", "abc\\$");
    }

    @Test
    void testCaretCaret() {
        testPattern("^^", "\\^\\^");
    }

    @Test
    void testCaretDollar() {
        testPattern("^$", "\\^\\$");
    }

    @Test
    void testDot() {
        testPattern(".", ".");
    }

    @Test
    void testNotColon() {
        testPattern("[^:]+", "[^:]+");
    }

    @Test
    void testDollar() {
        testPattern("$", "\\$");
    }

    @Test
    void testDollarOneDollar() {
        testPattern("$1$", "\\$1\\$");
    }

    @Test
    void testDollarPercentRange() {
        testPattern("[$-%]+", "[$-%]+");
    }

    @Test
    void testDollarRange() {
        testPattern("[$$]+", "[$$]+");
    }

    @Test
    void testDollarCaretRange() {
        testPattern("[$^]+", "[$^]+");
    }

    @Test
    void testSimple() {
        testPattern("abc", "abc");
    }

    @Test
    void testDotPlus() {
        testPattern(".+", ".+");
    }

    @Test
    void testDotStar() {
        testPattern(".*", ".*");
    }

    @Test
    void testSimpleOptional() {
        testPattern("a?", "a?");
    }

    @Test
    void testRangeOptional() {
        testPattern("[a-z]?", "[a-z]?");
    }

    @Test
    void testInvalidXSDRegexes() throws UnsupportedEncodingException {
        testInvalidPattern("$^a^[$^\\]", "Unclosed character class");
        testInvalidPattern("$(\\)", "Unclosed group");
    }

    @Test
    void testJavaPattern() {
        testPattern("^[$^]+$", ImmutableList.of("$^", "^", "$"), ImmutableList.of("\\", "a"));
        testPattern("^[^$-^]$", ImmutableList.of("a", "_", "#"), ImmutableList.of("%", "^", "$", "]", "\\"));
    }

    @Test
    void testJavaRegexFromXSD() {
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
            assertTrue(testMatch(javaRegex, value),
                "Value '" + value + "' does not match java regex '" + javaRegex + "'");
        }
        for (final String value : negativeMatches) {
            assertFalse(testMatch(javaRegex, value), "Value '" + value + "' matches java regex '" + javaRegex + "'");
        }
    }

    private static void testPattern(final String xsdRegex, final String expectedJavaRegex,
        final List<String> positiveMatches, final List<String> negativeMatches) {
        final String javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD(xsdRegex);
        assertEquals(expectedJavaRegex, javaRegexFromXSD);

        for (final String value : positiveMatches) {
            assertTrue(testMatch(javaRegexFromXSD, value),
                "Value '" + value + "' does not match java regex '" + javaRegexFromXSD + "'");
        }
        for (final String value : negativeMatches) {
            assertFalse(testMatch(javaRegexFromXSD, value),
                "Value '" + value + "' matches java regex '" + javaRegexFromXSD + "'");
        }
    }
}
