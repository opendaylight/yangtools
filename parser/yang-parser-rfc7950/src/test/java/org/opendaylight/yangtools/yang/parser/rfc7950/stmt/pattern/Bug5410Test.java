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
        testPattern("^[$^]+$", List.of("$^", "^", "$"), List.of("\\", "a"));
        testPattern("^[^$-^]$", List.of("a", "_", "#"), List.of("%", "^", "$", "]", "\\"));
    }

    @Test
    void testJavaRegexFromXSD() {
        testPattern("^[^:]+$", "^(?:\\^[^:]+\\$)$", List.of("^a$", "^abc$"), List.of("abc$", "^abc", "^a:bc$"));
        testPattern("^[$^]$", "^(?:\\^[$^]\\$)$", List.of("^^$", "^$$"), List.of("^^", "^$", "$^", "$$"));
        testPattern("[$-%]+", "^(?:[$-%]+)$", List.of("$", "%", "%$"), List.of("$-", "$-%", "-", "^"));
        testPattern("[$-&]+", "^(?:[$-&]+)$", List.of("$", "%&", "%$", "$%&"), List.of("#", "$-&", "'"));

        testPattern("[a-z&&[^m-p]]+", "^(?:[a-z&&[^m-p]]+)$", List.of("a", "z", "az"), List.of("m", "anz", "o"));
        testPattern("^[\\[-b&&[^^-a]]+$", "^(?:\\^[\\[-b&&[^^-a]]+\\$)$",
            List.of("^[$", "^\\$", "^]$", "^b$"), List.of("^a$", "^^$", "^_$"));

        // FIXME: YANGTOOLS-887: these patterns are not translated correctly, "&&" is a different construct in XSD
        //        testPattern("[^^-~&&[^$-^]]", "^(?:[^^-~&&[^$-^]])$",
        //            List.of("!", "\"", "#"), List.of("a", "A", "z", "Z", "$", "%", "^", "}"));
        //        testPattern("\\\\\\[^[^^-~&&[^$-^]]", "^(?:\\\\\\[\\^[^^-~&&[^$-^]])$",
        //            List.of("\\[^ ", "\\[^!", "\\[^\"", "\\[^#"),
        //            List.of("\\[^a", "\\[^A", "\\[^z", "\\[^Z", "\\[^$", "\\[^%", "\\[^^", "\\[^}"));
        //        testPattern("^\\[^\\\\[^^-b&&[^\\[-\\]]]\\]^", "^(?:\\^\\[\\^\\\\[^^-b&&[^\\[-\\]]]\\]\\^)$",
        //            List.of("^[^\\c]^", "^[^\\Z]^"),
        //            List.of("^[^\\[]^", "^[^\\\\]^", "^[^\\]]^", "^[^\\^]^", "^[^\\_]^", "^[^\\b]^"));
        //        testPattern("[\\^]$", "^(?:[\\^]\\$)$",
        //            List.of("^$"), List.of("^", "$", "$^", "\\", "\\^", "\\^\\", "\\^\\$"));
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
        testPattern(xsdRegex, "^(?:" + unanchoredJavaRegex + ")$", List.of(), List.of());
    }

    private static void testPattern(final String javaRegex, final List<String> positiveMatches,
            final List<String> negativeMatches) {
        for (var value : positiveMatches) {
            assertTrue(testMatch(javaRegex, value),
                "Value '" + value + "' does not match java regex '" + javaRegex + "'");
        }
        for (var value : negativeMatches) {
            assertFalse(testMatch(javaRegex, value), "Value '" + value + "' matches java regex '" + javaRegex + "'");
        }
    }

    private static void testPattern(final String xsdRegex, final String expectedJavaRegex,
            final List<String> positiveMatches, final List<String> negativeMatches) {
        final String javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD(xsdRegex);
        assertEquals(expectedJavaRegex, javaRegexFromXSD);

        for (var value : positiveMatches) {
            assertTrue(testMatch(javaRegexFromXSD, value),
                "Value '" + value + "' does not match java regex '" + javaRegexFromXSD + "'");
        }
        for (var value : negativeMatches) {
            assertFalse(testMatch(javaRegexFromXSD, value),
                "Value '" + value + "' matches java regex '" + javaRegexFromXSD + "'");
        }
    }
}
