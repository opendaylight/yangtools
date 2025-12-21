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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

class AbsoluteSchemaNodeidParserTest extends AbstractNamespaceBindingTest<Absolute> {
    private AbsoluteSchemaNodeidParser parser;

    @Override
    AbsoluteSchemaNodeidParser parser() {
        return parser;
    }

    @BeforeEach
    void beforeEach() {
        parser = new AbsoluteSchemaNodeidParser(new DescendantSchemaNodeidParser(new NodeIdentifierParser(
            new IdentifierParser(namespaceBinding))));
    }


    @Test
    void happyParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        doReturn(BAR).when(namespaceBinding).currentModule();
        assertArgument(Absolute.of(QName.create(FOO, "foolocal"), QName.create(BAR, "barlocal")),
            "/abc:foolocal/barlocal");
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertSyntaxException("");
        assertEquals("empty string", ex.getMessage());
        assertEquals(0, ex.getPosition());
    }

    @Test
    void notSlashParseArgumentThrows() {
        final var ex = assertSyntaxException("a");
        assertEquals("'a' is not '/'", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void slashParseArgumentThrows() {
        final var ex = assertSyntaxException("/");
        // FIXME: 'empty identifier" ?
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(2, ex.getPosition());
    }

    @Test
    void trailingSlashParseArgumentThrows() {
        doReturn(BAR).when(namespaceBinding).currentModule();
        final var ex = assertSyntaxException("/a/b/");
        // FIXME: 'empty identifier" ?
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(6, ex.getPosition());
    }
}
