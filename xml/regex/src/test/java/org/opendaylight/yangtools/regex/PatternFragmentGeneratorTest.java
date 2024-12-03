/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternFragmentGeneratorTest {
    @ParameterizedTest
    @MethodSource
    void positiveTest(final @NonNull String regex, final String expectedFragment) {
        assertEquals(expectedFragment,
            assertDoesNotThrow(() -> RegularExpressionParser.parse(regex).toPatternFragment()));
    }

    private static List<Arguments> positiveTest() {
        return List.of(
            arguments("", ""),
            arguments(".", "."),
            arguments(".*", ".*"),
            arguments("\n", "\n"),

            arguments("a", "a"),
            arguments("a+", "a+"),
            arguments("ab?", "ab?"),
            arguments("a|b", "a|b"),
            arguments("a{2}", "a{2}"),
            arguments("a{2,}", "a{2,}"),
            arguments("a{2,3}", "a{2,3}"),

            // Positive expressions
            arguments("[-]", "[-]"),
            arguments("[a]", "[a]"),
            arguments("[a-]", "[a-]"),
            arguments("[ab]", "[ab]"),
            arguments("[a-c]", "[a-c]"),
            arguments("[a-c-[b]]", "[a-c&&[^b]]"),
            arguments("[a-c-[^b]]", "[a-c&&[b]]"),
            arguments("[a--[b]]", "[a-&&[^b]]"),

            // Negative expressions
            arguments("[^-]", "[^-]"),
            arguments("[^a]", "[^a]"),
            arguments("[^a-]", "[^a-]"),
            arguments("[^ab]", "[^ab]"),
            arguments("[^a-c]", "[^a-c]"),
            arguments("[^a-c-[b]]", "[^a-c&&[^b]]"),
            arguments("[^a-c-[^b]]", "[^a-c&&[b]]"),
            arguments("[^a--[b]]", "[^a-&&[^b]]"),

            // Weird things
            arguments("|||", "|||"),
            arguments("()", "(?:)"));
    }
}
