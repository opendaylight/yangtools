/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class AnltrSupportTest {
    private static final @NonNull StatementSourceReference REF = StatementDeclarations.inText(1, 1);

    @ParameterizedTest
    @MethodSource
    void negativeTest(final @NonNull String regex, final String expectedMessage) {
        final var ex = assertThrows(SourceException.class, () -> RegularExpressionParser.parse(REF, regex));
        assertEquals(expectedMessage, ex.getMessage());
    }

    private static List<Arguments> negativeTest() {
        return List.of(
            Arguments.of("{", "extraneous input '{' expecting <EOF> at 1:0 [at <UNKNOWN>:1:1]"),
            Arguments.of("[", """
                mismatched input '<EOF>' expecting {SingleCharEsc, MultiCharEsc, CatEsc, ComplEsc, \
                NestedSingleCharEsc, NestedMultiCharEsc, NestedCatEsc, NestedComplEsc, '-', XmlChar} at 1:1 \
                [at <UNKNOWN>:1:1]"""),
            Arguments.of("a{", "mismatched input '<EOF>' expecting QuantExact at 1:2 [at <UNKNOWN>:1:1]"),
            Arguments.of("a{-1}", "token recognition error at: '-' at 1:2 [at <UNKNOWN>:1:1]"),
            Arguments.of("a{,2}", "extraneous input ',' expecting QuantExact at 1:2 [at <UNKNOWN>:1:1]"),
            Arguments.of("a{1x}", "token recognition error at: 'x' at 1:3 [at <UNKNOWN>:1:1]")
            );
    }
}
