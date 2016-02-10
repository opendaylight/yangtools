/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class Bug4079Test {

    @Test
    public void testValidPatternFix() {
        String fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsArrows})*+");
        assertEquals("(\\p{InArrows})*+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsDingbats})++");
        assertEquals("(\\p{InDingbats})++", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsSpecials})?+");
        assertEquals("(\\p{InSpecials})?+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsBatak}){4}+");
        assertEquals("(\\p{IsBatak}){4}+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsLatin}){4,6}+");
        assertEquals("(\\p{IsLatin}){4,6}+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsTibetan}){4,}+");
        assertEquals("(\\p{IsTibetan}){4,}+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsAlphabetic}){4}?");
        assertEquals("(\\p{IsAlphabetic}){4}?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsLowercase}){4,6}?");
        assertEquals("(\\p{IsLowercase}){4,6}?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsUppercase}){4,}?");
        assertEquals("(\\p{IsUppercase}){4,}?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement})*");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsBasicLatin}|\\p{InLatin-1Supplement})?");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}){4}");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement}){4}", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsLatin}|\\p{IsArmenian}){2,4}");
        assertEquals("(\\p{IsLatin}|\\p{IsArmenian}){2,4}", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsLatin}|\\p{IsBasicLatin}){2,}");
        assertEquals("(\\p{IsLatin}|\\p{InBasicLatin}){2,}", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsBasicLatin}|\\p{IsLatin})*?");
        assertEquals("(\\p{InBasicLatin}|\\p{IsLatin})*?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern(
                "(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsArrows})+?");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{InArrows})+?", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern(
                "(\\p{InBasicLatin}|\\p{IsLatin-1Supplement}|\\p{IsLatin})??");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement}|\\p{IsLatin})??", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\\\\\p{IsBasicLatin})*+");
        assertEquals("(\\\\\\p{InBasicLatin})*+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("(\\\\\\\\\\p{InBasicLatin})*+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\\\\\\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("(\\\\\\\\\\\\\\p{InBasicLatin})*+", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));
    }

    @Test(expected = PatternSyntaxException.class)
    public void testInvalidPattern() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\\\p{IsBasicLatin})*+");
        assertEquals("(\\\\p{IsBasicLatin})*+", fixedUnicodeScriptPattern);
        // should throw exception
        Pattern.compile(fixedUnicodeScriptPattern);
    }

    @Test(expected = PatternSyntaxException.class)
    public void testInvalidPattern2() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\p{IsSpecials}|\\\\\\\\p{IsBasicLatin})*+");
        assertEquals("(\\p{InSpecials}|\\\\\\\\p{IsBasicLatin})*+", fixedUnicodeScriptPattern);
        // should throw exception
        Pattern.compile(fixedUnicodeScriptPattern);
    }

    @Test(expected = PatternSyntaxException.class)
    public void testInvalidPattern3() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fixedUnicodeScriptPattern = Utils.fixUnicodeScriptPattern("(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+");
        assertEquals("(\\\\\\\\\\\\p{IsBasicLatin}|\\p{IsTags})*+", fixedUnicodeScriptPattern);
        // should throw exception
        Pattern.compile(fixedUnicodeScriptPattern);
    }
}
