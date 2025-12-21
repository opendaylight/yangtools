/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

class IdentifierParserTest extends AbstractNamespaceBindingTest {
    @Test
    void happyParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(QName.create(FOO, "aeiou"), parsers.identifier().parseArgument("aeiou"));
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertBadArgumentSyntax("");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void badParseArgumentThrows() {
        final var ex = assertBadArgumentSyntax("+");
        assertEquals("'+' is not a valid identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    private ArgumentSyntaxException assertBadArgumentSyntax(final String arg) {
        return assertSyntaxException(() -> parsers.identifier().parseArgument(arg));
    }
}
