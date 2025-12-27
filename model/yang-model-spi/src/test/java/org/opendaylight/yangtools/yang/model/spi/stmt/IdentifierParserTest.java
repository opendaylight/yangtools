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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

class IdentifierParserTest extends AbstractNamespaceBindingTest {
    private IdentifierParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new IdentifierParser(namespaceBinding);
    }

    @Test
    void goodParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(QName.create(FOO, "aeiou"), parser.parseArgument("aeiou"));
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertBadArgumentSyntax("");
        assertEquals("identifier-arg cannot be empty", ex.getMessage());
        assertEquals(0, ex.getPosition());
    }

    @Test
    void badStartParseArgumentThrows() {
        final var ex = assertBadArgumentSyntax("+");
        assertEquals("'+' is not valid as a first character in identifier-arg", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void badPartParseArgumentThrows() {
        final var ex = assertBadArgumentSyntax("abc]123");
        assertEquals("']' is not valid as a character in identifier-arg", ex.getMessage());
        assertEquals(4, ex.getPosition());
    }

    @Test
    void toStringReportsCurrentModule() {
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals("IdentifierParser{currentModule=QNameModule{ns=foons, rev=2025-12-16}}", parser.toString());
    }

    private ArgumentSyntaxException assertBadArgumentSyntax(final String arg) {
        return assertSyntaxException(() -> parser.parseArgument(arg));
    }
}
