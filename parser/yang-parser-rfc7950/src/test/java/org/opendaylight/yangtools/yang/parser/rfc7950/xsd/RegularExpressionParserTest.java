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

class RegularExpressionParserTest {
    private static final @NonNull StatementSourceReference REF = StatementDeclarations.inText(1, 1);

    @ParameterizedTest
    @MethodSource
    void positiveTest(final String regex, final RegularExpression expected) {
        assertEquals(expected, RegularExpressionParser.parse(REF, regex));
    }

    private static List<Arguments> positiveTest() {
        return List.of(
            // Simple things
            Arguments.of("", new RegularExpression(Branch.EMPTY)),
            Arguments.of(".", new RegularExpression(new Branch(new Piece(Dot.INSTANCE)))),
            Arguments.of(".*", new RegularExpression(new Branch(new Piece(Dot.INSTANCE, Star.INSTANCE)))),
            Arguments.of("\n", new RegularExpression(new Branch(new Piece(new NormalCharacter("\n"))))),

            // Quantifiers
            Arguments.of("a", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"))))),
            Arguments.of("a+", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), Plus.INSTANCE)))),
            Arguments.of("ab?", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a")),
                new Piece(new NormalCharacter("b"), Question.INSTANCE)))),
            Arguments.of("a|b", new RegularExpression(
                new Branch(new Piece(new NormalCharacter("a"))),
                new Branch(new Piece(new NormalCharacter("b"))))),
            Arguments.of("a{2}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantExact("2"))))),
            Arguments.of("a{2,}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantMin("2"))))),
            Arguments.of("a{2,3}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantRange("2", "3"))))),

            // Positive expressions
            Arguments.of("[-]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("-")))))),
            Arguments.of("[a]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("a")))))),
            Arguments.of("[a-]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("a-")))))),
            Arguments.of("[ab]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new UnionCharacterGroup(List.of(
                    new SimpleCharacterGroup("a"), new SimpleCharacterGroup("b")))))))),
            Arguments.of("[a-c]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new RangeCharacterGroup("a", "c")))))),
            Arguments.of("[a-c-[b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            Arguments.of("[a-c-[^b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new NegativeCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            Arguments.of("[a--[b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    // FIXME: this does not look right: it should be an open range [a-]
                    new SimpleCharacterGroup("a-"), new PositiveCharacterClassExpression(new SimpleCharacterGroup("b"))
                    )))))),

            // Negative expressions
            Arguments.of("[^-]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("-")))))),
            Arguments.of("[^a]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("a")))))),
            Arguments.of("[^a-]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("a-")))))),
            Arguments.of("[^ab]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new UnionCharacterGroup(List.of(
                    new SimpleCharacterGroup("a"), new SimpleCharacterGroup("b")))))))),
            Arguments.of("[^a-c]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new RangeCharacterGroup("a", "c")))))),
            Arguments.of("[^a-c-[b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            Arguments.of("[^a-c-[^b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new NegativeCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            Arguments.of("[^a--[b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    // FIXME: this does not look right: it should be an open range [a-]
                    new SimpleCharacterGroup("a-"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),

            // Weird things
            Arguments.of("|||", new RegularExpression(Branch.EMPTY, Branch.EMPTY, Branch.EMPTY, Branch.EMPTY)),

            // Nested expressions
            Arguments.of("()", new RegularExpression(new Branch(
                new Piece(new ParenRegularExpression(new RegularExpression(Branch.EMPTY))))))
            );
    }
}
