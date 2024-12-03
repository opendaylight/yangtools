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

class RegularExpressionParserTest {
    @ParameterizedTest
    @MethodSource
    void positiveTest(final @NonNull String regex, final RegularExpression expected) {
        assertEquals(expected, assertDoesNotThrow(() -> RegularExpressionParser.parse(regex)));
    }

    private static List<Arguments> positiveTest() {
        return List.of(
            // Simple things
            arguments("", new RegularExpression(Branch.EMPTY)),
            arguments(".", new RegularExpression(new Branch(new Piece(Dot.INSTANCE)))),
            arguments(".*", new RegularExpression(new Branch(new Piece(Dot.INSTANCE, Star.INSTANCE)))),
            arguments("\n", new RegularExpression(new Branch(new Piece(new NormalCharacter("\n"))))),

            // Quantifiers
            arguments("a", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"))))),
            arguments("a+", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), Plus.INSTANCE)))),
            arguments("ab?", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a")),
                new Piece(new NormalCharacter("b"), Question.INSTANCE)))),
            arguments("a|b", new RegularExpression(
                new Branch(new Piece(new NormalCharacter("a"))),
                new Branch(new Piece(new NormalCharacter("b"))))),
            arguments("a{2}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantExact("2"))))),
            arguments("a{2,}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantMin("2"))))),
            arguments("a{2,3}", new RegularExpression(new Branch(
                new Piece(new NormalCharacter("a"), new QuantRange("2", "3"))))),

            // Positive expressions
            arguments("[-]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("-")))))),
            arguments("[a]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("a")))))),
            arguments("[a-]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new SimpleCharacterGroup("a-")))))),
            arguments("[ab]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new UnionCharacterGroup(List.of(
                    new SimpleCharacterGroup("a"), new SimpleCharacterGroup("b")))))))),
            arguments("[a-c]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new RangeCharacterGroup("a", "c")))))),
            arguments("[a-c-[b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            arguments("[a-c-[^b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new NegativeCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            arguments("[a--[b]]", new RegularExpression(new Branch(
                new Piece(new PositiveCharacterClassExpression(new DifferenceCharacterGroup(
                    // FIXME: this does not look right: it should be an open range [a-]
                    new SimpleCharacterGroup("a-"), new PositiveCharacterClassExpression(new SimpleCharacterGroup("b"))
                    )))))),

            // Negative expressions
            arguments("[^-]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("-")))))),
            arguments("[^a]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("a")))))),
            arguments("[^a-]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new SimpleCharacterGroup("a-")))))),
            arguments("[^ab]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new UnionCharacterGroup(List.of(
                    new SimpleCharacterGroup("a"), new SimpleCharacterGroup("b")))))))),
            arguments("[^a-c]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new RangeCharacterGroup("a", "c")))))),
            arguments("[^a-c-[b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            arguments("[^a-c-[^b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    new RangeCharacterGroup("a", "c"),
                    new NegativeCharacterClassExpression(new SimpleCharacterGroup("b")))))))),
            arguments("[^a--[b]]", new RegularExpression(new Branch(
                new Piece(new NegativeCharacterClassExpression(new DifferenceCharacterGroup(
                    // FIXME: this does not look right: it should be an open range [a-]
                    new SimpleCharacterGroup("a-"),
                    new PositiveCharacterClassExpression(new SimpleCharacterGroup("b")))))))),

            // Weird things
            arguments("|||", new RegularExpression(Branch.EMPTY, Branch.EMPTY, Branch.EMPTY, Branch.EMPTY)),

            // Nested expressions
            arguments("()", new RegularExpression(new Branch(
                new Piece(new ParenRegularExpression(new RegularExpression(Branch.EMPTY)))))));
    }
}
