/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

class ImmutableDocumentTest {
    @Test
    void emptyStreamThrows() {
        final var ex = assertThrows(XMLStreamException.class, () -> Document.of(""));
        assertEquals("""
            ParseError at [row,col]:[1,1]
            Message: Premature end of file.""", ex.getMessage());
    }

    @Test
    void nakedElement() throws Exception {
        final var doc = assertDocument("<foo/>");
        assertEquals("ImmutableContainerElement{name=foo}", doc.toString());
    }

    @Test
    void nakedElementDocument() throws Exception {
        final var doc = assertDocument("""
            <?xml version="1.0" encoding="UTF-8"?>
            <foo/>""");
        assertEquals("ImmutableContainerElement{name=foo}", doc.toString());
    }

    @Test
    void emptyElement() throws Exception {
        final var doc = assertDocument("""
            <foo xmlns="namespace"/>""");
        assertEquals("ImmutableContainerElement{name=(namespace)foo, attrs=1}", doc.toString());
        assertEquals("namespace", doc.attributeValue(null, "xmlns"));
        assertInstanceOf(DefaultNamespaceDeclaration.class, doc.attribute(null, "xmlns"));
    }

    private static Element assertDocument(final String str) {
        final Document doc;
        try {
            doc = Document.of(str);
        } catch (XMLStreamException e) {
            throw new AssertionError(e);
        }

        final var element = doc.element();
        assertNotNull(element);
        return element;
    }
}
