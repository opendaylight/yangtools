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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

@ExtendWith(MockitoExtension.class)
class RegularExpressionParserTest {
    @Mock
    private StatementSourceReference ref;

    @ParameterizedTest
    @MethodSource
    void testParse(final String regex, final RegularExpression expected) {
        assertEquals(expected, RegularExpressionParser.parse(ref, regex));
    }

    private static List<Arguments> testParse() {
        return List.of(
            Arguments.of("", new RegularExpression(new Branch())),
            Arguments.of("a", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"))))),
            Arguments.of("ab?", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a")),
                new Piece(new NormalCharacter("b"), Question.INSTANCE)))),
            Arguments.of("a|b", new RegularExpression(
                new Branch(new Piece(new NormalCharacter("a"))),
                new Branch(new Piece(new NormalCharacter("b"))))),
            Arguments.of("a+", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), Plus.INSTANCE)))),
            Arguments.of("a{2}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantExact("2"))))),
            Arguments.of("a{2,}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantMin("2"))))),
            Arguments.of("a{2,3}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantRange("2", "3"))))));
    }
}
