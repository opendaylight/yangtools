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

class NodeIdentifierParserTest extends AbstractNamespaceBindingTest<QName> {
    private NodeIdentifierParser parser;

    @Override
    NodeIdentifierParser parser() {
        return parser;
    }

    @BeforeEach
    void beforeEach() {
        parser = new NodeIdentifierParser(new IdentifierParser(namespaceBinding));
    }

    @Test
    void goodParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(QName.create(FOO, "aeiou"), parser.parseArgument("aeiou"));
    }

    @Test
    void goodPrefixParseArgument() throws Exception {
        doReturn(BAR).when(namespaceBinding).lookupModule(ABC);
        assertEquals(QName.create(BAR, "aeiou"), parser.parseArgument("abc:aeiou"));
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertSyntaxException("");
        assertEquals("node-identifier cannot be empty", ex.getMessage());
        assertEquals(0, ex.getPosition());
    }

    @Test
    void badParseArgumentThrows() {
        final var ex = assertSyntaxException("+");
        assertEquals("'+' is not valid as a first character in identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void badPrefixParseArgumentThrows() {
        final var ex = assertSyntaxException("+:a");
        assertEquals("'+' is not valid as a first character in prefix", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void badIdentifierParseArgumentThrows() {
        doReturn(FOO).when(namespaceBinding).lookupModule(ABC);
        final var ex = assertSyntaxException("abc:+");
        assertEquals("'+' is not valid as a first character in identifier", ex.getMessage());
        assertEquals(5, ex.getPosition());
    }

    @Test
    void emptyNodeIdentifierThrows() {
        doReturn(FOO).when(namespaceBinding).lookupModule(ABC);
        final var ex = assertSyntaxException("abc:");
        assertEquals("identifier cannot be empty", ex.getMessage());
        assertEquals(4, ex.getPosition());
    }

    @Test
    void emptyPrefixThrows() {
        final var ex = assertSyntaxException(":abc");
        assertEquals("':' is not a valid prefix nor identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void unknownModuleParseNodeIdentifierThrows() {
        doReturn(null).when(namespaceBinding).lookupModule(ABC);
        final var ex = assertBindingException("abc:a");
        assertEquals("Prefix 'abc' cannot be resolved", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void toStringReportsCurrentModule() {
        assertEquals("NodeIdentifierParser{namespaceBinding=namespaceBinding}", parser.toString());
    }
}
