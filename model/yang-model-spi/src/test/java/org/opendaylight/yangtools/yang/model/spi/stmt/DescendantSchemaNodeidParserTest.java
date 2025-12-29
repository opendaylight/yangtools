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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
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
    void goodOneParseArgument() throws Exception {
        doReturn(BAR).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(Descendant.of(QName.create(BAR, "foolocal"), QName.create(FOO, "barlocal")),
            parser.parseArgument("abc:foolocal/barlocal"));
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertSyntaxException("");
        assertEquals("descendant-schema-nodeid cannot be empty", ex.getMessage());
        assertEquals(0, ex.getPosition());
    }

    @Test
    void slashParseArgumentThrows() {

    }
}
