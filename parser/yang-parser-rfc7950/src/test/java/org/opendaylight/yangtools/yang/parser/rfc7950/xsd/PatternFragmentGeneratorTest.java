/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;

class PatternFragmentGeneratorTest {
    private static final @NonNull StatementSourceReference REF = StatementDeclarations.inText(1, 1);

    @ParameterizedTest
    @MethodSource
    void positiveTest(final @NonNull String regex, final String expectedFragment) {
        assertEquals(expectedFragment, RegularExpressionParser.parse(REF, regex).toPatternFragment());
    }

    private static List<Arguments> positiveTest() {
        return List.of(
            Arguments.of("", ""),
            Arguments.of(".", "."),
            Arguments.of(".*", ".*"),
            Arguments.of("\n", "\n"),

            Arguments.of("a", "a"),
            Arguments.of("a+", "a+"),
            Arguments.of("ab?", "ab?"),
            Arguments.of("a|b", "a|b"),
            Arguments.of("a{2}", "a{2}"),
            Arguments.of("a{2,}", "a{2,}"),
            Arguments.of("a{2,3}", "a{2,3}"),

            // Positive expressions
            Arguments.of("[-]", "[-]"),
            Arguments.of("[a]", "[a]"),
            Arguments.of("[a-]", "[a-]"),
            Arguments.of("[ab]", "[ab]"),
            Arguments.of("[a-c]", "[a-c]"),
            Arguments.of("[a-c-[b]]", "[a-c&&[^b]]"),
            Arguments.of("[a-c-[^b]]", "[a-c&&[b]]"),
            Arguments.of("[a--[b]]", "[a-&&[^b]]"),

            // Negative expressions
            Arguments.of("[^-]", "[^-]"),
            Arguments.of("[^a]", "[^a]"),
            Arguments.of("[^a-]", "[^a-]"),
            Arguments.of("[^ab]", "[^ab]"),
            Arguments.of("[^a-c]", "[^a-c]"),
            Arguments.of("[^a-c-[b]]", "[^a-c&&[^b]]"),
            Arguments.of("[^a-c-[^b]]", "[^a-c&&[b]]"),
            Arguments.of("[^a--[b]]", "[^a-&&[^b]]"),

            // Weird things
            Arguments.of("|||", "|||"),
            Arguments.of("()", "(?:)")
            );
    }
}
