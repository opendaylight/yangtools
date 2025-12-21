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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

class NodeIdentifierParserTest extends AbstractNamespaceBindingTest {
    @Override
    void happyParseArgument() throws Exception {
        doReturn(FOO).when(namespaceBinding).currentModule();
        assertEquals(QName.create(FOO, "aeiou"), parsers.identifier().parseArgument("aeiou"));
    }

    @Test
    void badParseArgumentThrows() {
        final var ex = assertSyntaxException("+");
        assertEquals("'+' is not a valid identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void emptyParseArgumentThrows() {
        final var ex = assertSyntaxException("");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void emptyNodeIdentifierThrows() {
        doReturn(FOO).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        final var ex = assertSyntaxException("abc:");
        assertEquals("'' is not a valid identifier", ex.getMessage());
        assertEquals(5, ex.getPosition());
    }

    @Test
    void emptyPrefixThrows() {
        final var ex = assertSyntaxException(":abc");
        assertEquals("':' is not a valid prefix nor identifier", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    @Test
    void unknownModuleParseNodeIdentifierThrows() {
        doReturn(null).when(namespaceBinding).lookupModule(Unqualified.of("abc"));
        final var ex = assertBindingException("abc:");
        assertEquals("Prefix 'abc' cannot be resolved", ex.getMessage());
        assertEquals(1, ex.getPosition());
    }

    private ArgumentSyntaxException assertSyntaxException(final String arg) {
        return assertSyntaxException(() -> parsers.nodeIdentifier().parseArgument(arg));
    }

    private ArgumentBindingException assertBindingException(final String arg) {
        return assertBindingException(() -> parsers.nodeIdentifier().parseArgument(arg));
    }
}
