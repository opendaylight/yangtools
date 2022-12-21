/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.Test;

class Bug4079Test {
    @Test
    void testValidPatternFix() {
        assertJavaRegex("^(?:(\\p{InArrows})*+)$", "(\\p{IsArrows})*+");
        assertJavaRegex("^(?:(\\p{InDingbats})++)$", "(\\p{IsDingbats})++");
        assertJavaRegex("^(?:(\\p{InSpecials})?+)$", "(\\p{IsSpecials})?+");
        assertJavaRegex("^(?:(\\p{IsBatak}){4}+)$", "(\\p{IsBatak}){4}+");
        assertJavaRegex("^(?:(\\p{IsLatin}){4,6}+)$", "(\\p{IsLatin}){4,6}+");
        assertJavaRegex("^(?:(\\p{IsTibetan}){4,}+)$", "(\\p{IsTibetan}){4,}+");
        assertJavaRegex("^(?:(\\p{IsAlphabetic}){4}?)$", "(\\p{IsAlphabetic}){4}?");
        assertJavaRegex("^(?:(\\p{IsLowercase}){4,6}?)$", "(\\p{IsLowercase}){4,6}?");
        assertJavaRegex("^(?:(\\p{IsUppercase}){4,}?)$", "(\\p{IsUppercase}){4,}?");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})*)$",
            "(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement})*");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+)$",
            "(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})?)$",
            "(\\p{IsBasicLatin}|\\p{InLatin-1Supplement})?");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}){4})$",
            "(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}){4}");
        assertJavaRegex("^(?:(\\p{IsLatin}|\\p{IsArmenian}){2,4})$", "(\\p{IsLatin}|\\p{IsArmenian}){2,4}");
        assertJavaRegex("^(?:(\\p{IsLatin}|\\p{InBasicLatin}){2,})$", "(\\p{IsLatin}|\\p{IsBasicLatin}){2,}");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{IsLatin})*?)$", "(\\p{IsBasicLatin}|\\p{IsLatin})*?");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{InArrows})+?)$",
            "(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsArrows})+?");
        assertJavaRegex("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{IsLatin})??)$",
            "(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsLatin})??");
        assertJavaRegex("^(?:(\\\\\\p{InBasicLatin})*+)$", "(\\\\\\p{IsBasicLatin})*+");
        assertJavaRegex("^(?:(\\\\\\\\\\p{InBasicLatin})*+)$", "(\\\\\\\\\\p{IsBasicLatin})*+");
        assertJavaRegex("^(?:(\\\\\\\\\\\\\\p{InBasicLatin})*+)$", "(\\\\\\\\\\\\\\p{IsBasicLatin})*+");
    }

    @Test
    void testInvalidPattern() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\\\p{IsBasicLatin})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    void testInvalidPattern2() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
            "(\\p{IsSpecials}|\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\p{InSpecials}|\\\\\\\\p{IsBasicLatin})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    void testInvalidPattern3() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
            "(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+");
        assertEquals("^(?:(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    void testCorrectBranches() {
        String str = RegexUtils.getJavaRegexFromXSD("a|bb");
        assertEquals("^(?:a|bb)$", str);
        Predicate<String> pred = Pattern.compile(str).asPredicate();

        assertTrue(pred.test("a"));
        assertTrue(pred.test("bb"));
        assertFalse(pred.test("ab"));
        assertFalse(pred.test("abb"));
        assertFalse(pred.test("ac"));
    }

    private static void assertJavaRegex(final String expected, final String xsdRegex) {
        final var actual = RegexUtils.getJavaRegexFromXSD(xsdRegex);
        assertEquals(expected, actual);
        assertNotNull(Pattern.compile(actual));
    }
}
