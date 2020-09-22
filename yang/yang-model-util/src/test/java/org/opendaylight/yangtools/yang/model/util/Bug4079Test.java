/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.Test;

public class Bug4079Test {

    @Test
    public void testValidPatternFix() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsArrows})*+");
        assertEquals("^(?:(\\p{InArrows})*+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsDingbats})++");
        assertEquals("^(?:(\\p{InDingbats})++)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsSpecials})?+");
        assertEquals("^(?:(\\p{InSpecials})?+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsBatak}){4}+");
        assertEquals("^(?:(\\p{IsBatak}){4}+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsLatin}){4,6}+");
        assertEquals("^(?:(\\p{IsLatin}){4,6}+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsTibetan}){4,}+");
        assertEquals("^(?:(\\p{IsTibetan}){4,}+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsAlphabetic}){4}?");
        assertEquals("^(?:(\\p{IsAlphabetic}){4}?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsLowercase}){4,6}?");
        assertEquals("^(?:(\\p{IsLowercase}){4,6}?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsUppercase}){4,}?");
        assertEquals("^(?:(\\p{IsUppercase}){4,}?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement})*");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})*)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsBasicLatin}|\\p{InLatin-1Supplement})?");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement})?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}){4}");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}){4})$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsLatin}|\\p{IsArmenian}){2,4}");
        assertEquals("^(?:(\\p{IsLatin}|\\p{IsArmenian}){2,4})$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsLatin}|\\p{IsBasicLatin}){2,}");
        assertEquals("^(?:(\\p{IsLatin}|\\p{InBasicLatin}){2,})$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\p{IsBasicLatin}|\\p{IsLatin})*?");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{IsLatin})*?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
                "(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsArrows})+?");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{InArrows})+?)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
                "(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsLatin})??");
        assertEquals("^(?:(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{IsLatin})??)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\\\\\p{InBasicLatin})*+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\\\\\\\\\p{InBasicLatin})*+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\\\\\\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\\\\\\\\\\\\\p{InBasicLatin})*+)$", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    public void testInvalidPattern() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD("(\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\\\p{IsBasicLatin})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    public void testInvalidPattern2() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
            "(\\p{IsSpecials}|\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("^(?:(\\p{InSpecials}|\\\\\\\\p{IsBasicLatin})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    public void testInvalidPattern3() {
        String fixedUnicodeScriptPattern = RegexUtils.getJavaRegexFromXSD(
            "(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+");
        assertEquals("^(?:(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+)$", fixedUnicodeScriptPattern);
        // should throw exception
        assertThrows(PatternSyntaxException.class, () -> Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test
    public void testCorrectBranches() {
        String str = RegexUtils.getJavaRegexFromXSD("a|bb");
        assertEquals("^(?:a|bb)$", str);
        Predicate<String> pred = Pattern.compile(str).asPredicate();

        assertTrue(pred.test("a"));
        assertTrue(pred.test("bb"));
        assertFalse(pred.test("ab"));
        assertFalse(pred.test("abb"));
        assertFalse(pred.test("ac"));
    }
}
