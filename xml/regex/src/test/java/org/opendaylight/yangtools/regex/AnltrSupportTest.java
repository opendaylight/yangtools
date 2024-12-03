/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnltrSupportTest {
    @ParameterizedTest
    @MethodSource
    void negativeTest(final @NonNull String regex, final String message, final int line, final int charPositionInLine) {
        final var ex = assertThrows(RegularExpressionException.class, () -> RegularExpressionParser.parse(regex));
        assertEquals(message, ex.getMessage());
        assertEquals(line, ex.line());
        assertEquals(charPositionInLine, ex.charPositionInLine());
    }

    private static List<Arguments> negativeTest() {
        return List.of(
            arguments("{", "extraneous input '{' expecting <EOF>",  1, 0),
            arguments("[", """
                mismatched input '<EOF>' expecting {SingleCharEsc, MultiCharEsc, CatEsc, ComplEsc, \
                NestedSingleCharEsc, NestedMultiCharEsc, NestedCatEsc, NestedComplEsc, '-', XmlChar}""", 1, 1),
            arguments("a{", "mismatched input '<EOF>' expecting QuantExact", 1, 2),
            arguments("a{-1}", "token recognition error at: '-'", 1, 2),
            arguments("a{,2}", "extraneous input ',' expecting QuantExact", 1, 2),
            arguments("a{1x}", "token recognition error at: 'x'", 1, 3));
    }
}
