/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class YT1571Test {

    @Test
    void characterSubtractionGroupTest() {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("[a-z-[c]]");
        assertEquals("^(?:[a-z&&[^c]])$", javaRegexFromXSD);
        final var pattern = Pattern.compile(javaRegexFromXSD);
        assertNotNull(pattern);
    }

    @Test
    void multipleCharacterSubtractionGroupTest() {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("c-[1-9-[3]][a-z-[cd]][^a-z-[123]]z");
        assertEquals("^(?:c-[1-9&&[^3]][a-z&&[^cd]][^a-z&&[^123]]z)$", javaRegexFromXSD);
        final var pattern = Pattern.compile(javaRegexFromXSD);
        assertNotNull(pattern);
        assertPatternMatch(pattern, new CharSequence[]{"c-4z4z", "c-1a9z"});
        assertPatternNotMatch(pattern, new CharSequence[]{"c-3a9z", "c-1d9z", "c-1aaz"});
    }

    @Test
    void multiCharEscapeSubtractionGroupWithDotTest() {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("\\.-\\[[\\w-[:]][.-[a-z]][.-[\\d]]]");
        assertEquals("^(?:\\.-\\[[\\w&&[^:]][^[a-z].][^[\\d].]])$", javaRegexFromXSD);
        final var pattern = Pattern.compile(javaRegexFromXSD);
        assertPatternMatch(pattern, new CharSequence[]{".-[AAA]", ".-[a1?]", ".-[1!a]"});
        assertPatternNotMatch(pattern, new CharSequence[]{"a-[SAa]", ".-[Sa\\n]", ".-[AaA]", ".-[AA2]", ".-[:Aa]"});
    }

    @Test
    void multipleCharacterSubtractionGroupAsStingTest() {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("\\\\i");
        assertEquals("^(?:\\\\i)$", javaRegexFromXSD);
        final var pattern = Pattern.compile(javaRegexFromXSD);
        assertPatternMatch(pattern, new CharSequence[]{"\\i"});
        assertPatternNotMatch(pattern, new CharSequence[]{"i", "[:_A-Za-z]", "\\\\i"});
    }

    @ParameterizedTest
    @MethodSource("multiCharEscapeArgs")
    void multiCharEscapeTest(final char ch, final CharSequence[] positiveMatch, final CharSequence[] negativeMatch) {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("\\" + ch);
        final var positivePattern = Pattern.compile(javaRegexFromXSD);
        assertNotNull(positivePattern);
        assertPatternMatch(positivePattern, positiveMatch);
        assertPatternNotMatch(positivePattern, negativeMatch);

        final var negativeJavaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("\\" + Character.toUpperCase(ch));
        final var negativePattern = Pattern.compile(negativeJavaRegexFromXSD);
        assertNotNull(negativePattern);
        assertPatternMatch(negativePattern, negativeMatch);
        assertPatternNotMatch(negativePattern, positiveMatch);
    }

    /*
     * Stream<Arguments> with:
     *  ch -> multi-character escape char https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#dt-ccesN
     *  positiveMatch - > Match for lower-case 'ch' and should not match upper-case 'ch'
     *  negativeMatch -> Match for upper-case 'ch' and should not match lower-case 'ch'
     */
    private static Stream<Arguments> multiCharEscapeArgs() {
        return Stream.of(
            Arguments.of('s', new CharSequence[]{"\t", "\n", "\r", " "}, new CharSequence[]{"1", "a"}),
            Arguments.of('i', new CharSequence[]{":", "_", "a"}, new CharSequence[]{"?", "1"}),
            Arguments.of('c', new CharSequence[]{"˿", "⁀", ":", "-", "a"}, new CharSequence[]{"!", "&"}),
            Arguments.of('d', new CharSequence[]{"1"}, new CharSequence[]{"a", ":"}),
            Arguments.of('w', new CharSequence[]{"a", "1", "_"}, new CharSequence[]{"?", "!"})
        );
    }

    private void assertPatternMatch(final Pattern pattern, final CharSequence[] match) {
        for (final var c : match) {
            assertTrue(pattern.matcher(c).find(), String.format("CharSequence %s do not match pattern %s", c, pattern));
        }
    }

    private void assertPatternNotMatch(final Pattern pattern, final CharSequence[] notMatch) {
        for (final var c : notMatch) {
            assertFalse(pattern.matcher(c).find(), String.format("CharSequence %s should not match pattern %s", c,
                pattern));
        }
    }
}
