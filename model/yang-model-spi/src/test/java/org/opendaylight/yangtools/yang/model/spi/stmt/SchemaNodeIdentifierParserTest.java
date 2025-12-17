/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.spi.stmt.SchemaNodeIdentifierParser.ModuleResolver;

@ExtendWith(MockitoExtension.class)
class SchemaNodeIdentifierParserTest {
    private static final QNameModule FOO = QNameModule.ofRevision("foons", "2025-12-16");
    private static final QNameModule BAR = QNameModule.ofRevision("barns", "2025-12-16");

    @Mock
    private ModuleResolver resolver;

    private SchemaNodeIdentifierParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new SchemaNodeIdentifierParser(resolver);
    }

    @Test
    void happyParseIdentifier() throws Exception {
        doReturn(FOO).when(resolver).currentModule();
        assertEquals(QName.create(FOO, "aeiou"), parser.parseIdentifier("aeiou"));
    }

    @Test
    void emptyParseIdentifierThrows() {
        final var ex = assertBadIdentifier("");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void badParseIdentifierThrows() {
        final var ex = assertBadIdentifier("+");
        assertEquals("'+' is not a valid identifier", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void emptyParseNodeIdentifierThrows() {
        final var ex = assertBadNodeIdentifier("");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void emptyPrefixParseNodeIdentifierThrows() {
        final var ex = assertBadNodeIdentifier(":abc");
        assertEquals("'' is not a valid prefix", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void emptyIdentifierParseNodeIdentifierThrows() {
        doReturn(FOO).when(resolver).lookupModule(Unqualified.of("abc"));
        final var ex = assertBadNodeIdentifier("abc:");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(4, ex.getErrorOffset());
    }

    @Test
    void unknownModuleParseNodeIdentifierThrows() {
        doReturn(null).when(resolver).lookupModule(Unqualified.of("abc"));
        final var ex = assertBadNodeIdentifier("abc:");
        assertEquals("Prefix 'abc' cannot be resolved", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void happyParseAbsoluteSchemaNodeid() throws Exception {
        doReturn(FOO).when(resolver).lookupModule(Unqualified.of("abc"));
        doReturn(BAR).when(resolver).currentModule();
        assertEquals(Absolute.of(QName.create(FOO, "foolocal"), QName.create(BAR, "barlocal")),
            parser.parseAbsoluteSchemaNodeid("/abc:foolocal/barlocal"));
    }

    @Test
    void happyParseDescendantSchemaNodeid() throws Exception {
        doReturn(BAR).when(resolver).lookupModule(Unqualified.of("abc"));
        doReturn(FOO).when(resolver).currentModule();
        assertEquals(Descendant.of(QName.create(BAR, "foolocal"), QName.create(FOO, "barlocal")),
            parser.parseDescendantSchemaNodeid("abc:foolocal/barlocal"));
    }

    private ParseException assertBadIdentifier(final String arg) {
        return assertParseException(() -> parser.parseIdentifier(arg));
    }

    private ParseException assertBadNodeIdentifier(final String arg) {
        return assertParseException(() -> parser.parseNodeIdentifier(arg));
    }

    private static ParseException assertParseException(final Executable executable) {
        return assertThrowsExactly(ParseException.class, executable);
    }
}
