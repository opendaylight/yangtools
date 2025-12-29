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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

class DescendantSchemaNodeidParserTest extends AbstractNamespaceBindingTest<Descendant> {
    private DescendantSchemaNodeidParser parser;

    @Override
    DescendantSchemaNodeidParser parser() {
        return parser;
    }

    @BeforeEach
    void beforeEach() {
        parser = new DescendantSchemaNodeidParser(new NodeIdentifierParser(new IdentifierParser(namespaceBinding)));
    }

    @Test
    void goodOneParseArgument() {
        doReturn(BAR).when(namespaceBinding).lookupModule(ABC);
        assertArgument(Descendant.of(QName.create(BAR, "foolocal")), "abc:foolocal");
    }

    @Test
    void goodTwoParseArgument() {
        doReturn(BAR).when(namespaceBinding).lookupModule(ABC);
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertArgument(Descendant.of(QName.create(BAR, "foolocal"), QName.create(FOO, "barlocal")),
            "abc:foolocal/barlocal");
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertSyntaxException("");
        assertEquals("descendant-schema-nodeid cannot be empty", ex.getMessage());
        assertEquals(0, ex.getPosition());
    }

    @Test
    void slashParseArgumentThrows() {
        final var ex = assertSyntaxException("/");
        assertEquals("'/' is not a valid prefix nor identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void trailingSlashParseArgumentThrows() {
        doReturn(FOO).when(namespaceBinding).lookupModule(ABC);
        final var ex = assertSyntaxException("abc:def/");
        assertEquals("node-identifier cannot be empty", ex.getMessage());
        assertEquals(8, ex.getPosition());
    }

    @Test
    void toStringReportsCurrentModule() {
        assertEquals("DescendantSchemaNodeidParser{namespaceBinding=namespaceBinding}", parser.toString());
    }
}
