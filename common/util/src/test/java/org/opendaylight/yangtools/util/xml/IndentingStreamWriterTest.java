/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndentingStreamWriterTest {
    private final StringWriter stream = new StringWriter();

    private IndentingStreamWriter writer;

    @BeforeEach
    private void beforeEach() throws Exception {
        writer = assertInstanceOf(IndentingStreamWriter.class,
            IndentedXML.of().wrapStreamWriter(XMLOutputFactory.newDefaultFactory().createXMLStreamWriter(stream)));
    }

    @Test
    void commentThenDocument() throws Exception {
        writer.writeComment("some\nmultiline\ncomment");
        writer.writeStartDocument();
        writer.writeEndDocument();
        writer.close();
        assertEquals("""
            <!--some
            multiline
            comment-->
            <?xml version="1.0" ?>""", stream.toString());
    }

    @Test
    void bomThenDocument() throws Exception {
        writer.writeCharacters(new char[] { 0xEF, 0xBB, 0xBF }, 0, 3);
        writer.writeStartDocument();
        writer.writeEndDocument();
        writer.close();
        assertEquals("ï»¿<?xml version=\"1.0\" ?>", stream.toString());
    }

    @Test
    void tooLazyToCloseElements() throws Exception {
        writer.writeStartDocument();
        writer.writeStartElement("foo");
        writer.writeStartElement("bar");
        writer.writeStartElement("baz");
        writer.writeEndDocument();
        writer.close();

        assertEquals("""
            <?xml version="1.0" ?>
            <foo>
              <bar>
                <baz></baz>
              </bar>
            </foo>""", stream.toString());
    }

    @Test
    void mixedContentThenMarkup() throws Exception {
        writer.writeStartDocument();
        writer.writeStartElement("foo");
        writer.writeCharacters("bar");
        writer.writeEmptyElement("baz");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        // Note: best effort to stop indenting
        assertEquals("""
            <?xml version="1.0" ?>
            <foo>bar<baz/></foo>""", stream.toString());
    }

    @Test
    void mixedMarkupThenContent() throws Exception {
        writer.writeStartDocument();
        writer.writeStartElement("p", "foo", "ns");
        writer.writeNamespace("p", "ns");
        writer.writeEmptyElement("bar");
        writer.writeCharacters("baz");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        // Note: best effort to stop indenting, but the content of foo is slightly different
        assertEquals("""
            <?xml version="1.0" ?>
            <p:foo xmlns:p="ns">
              <bar/>baz</p:foo>""", stream.toString());
    }
}
