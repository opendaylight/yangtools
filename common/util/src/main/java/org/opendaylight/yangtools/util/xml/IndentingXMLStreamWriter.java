/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A {@link XMLStreamWriter} decorator delegating to another {@link XMLStreamWriter}, adding whitespace to make the
 * resulting document more human-friendly. This <b>will likely corrupt mixed-content XML</b>, i.e. when an element
 * contains both other elements and data. Nevertheless, it works quite well for XML documents containing YANG-modeled
 * data.
 *
 * <p>We differentiate between two types of events:
 * <ol>
 *   <li>XML markup, like elements, comments, DTDs and processing instructions</li>
 *   <li>XML data, like characters, CDATA and entity references</li>
 * </ol>
 * We aim to start every markup on its own line, preceded by whitespace corresponding to the depth it is nested within
 * the document element.
 */
public final class IndentingXMLStreamWriter implements XMLStreamWriter {
    private static final int DEFAULT_INDENT = 2;
    private static final int DEFAULT_STACK_SIZE = 8;
    private static final int HAVE_MARKUP = 1;
    private static final int HAVE_DATA = 2;
    private static final int INDENT_CAPACITY = 128;
    private static final char[] INDENT_CHARS = ("\n" + " ".repeat(INDENT_CAPACITY)).toCharArray();

    private final XMLStreamWriter delegate;
    private final int indent;

    private int[] stack = new int[DEFAULT_STACK_SIZE];
    private int depth = 0;

    public IndentingXMLStreamWriter(final XMLStreamWriter delegate) {
        this(delegate, DEFAULT_INDENT);
    }

    public IndentingXMLStreamWriter(final XMLStreamWriter delegate, final int indent) {
        this.delegate = requireNonNull(delegate);
        if (indent < 1) {
            throw new IllegalArgumentException("non-positive indent " + indent);
        }
        this.indent = indent;
    }

    @Override
    public void writeStartElement(final String localName) throws XMLStreamException {
        preElement();
        delegate.writeStartElement(localName);
        postElement();
    }

    @Override
    public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
        preElement();
        delegate.writeStartElement(namespaceURI, localName);
        postElement();
    }

    @Override
    public void writeStartElement(final String prefix, final String localName, final String namespaceURI)
            throws XMLStreamException {
        preElement();
        delegate.writeStartElement(prefix, localName, namespaceURI);
        postElement();
    }

    @Override
    public void writeEmptyElement(final String localName) throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(localName);
        haveMarkup();
    }

    @Override
    public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(namespaceURI, localName);
        haveMarkup();
    }

    @Override
    public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI)
            throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(prefix, localName, namespaceURI);
        haveMarkup();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        final int next = depth - 1;
        if (stack[depth] == HAVE_MARKUP) {
            writeIndent(next);
        }
        delegate.writeEndElement();
        depth = next;
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        while (depth > 0) {
            writeEndElement();
        }
        delegate.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        delegate.close();
        stack = null;
    }

    @Override
    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    @Override
    public void writeAttribute(final String localName, final String value) throws XMLStreamException {
        delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(final String prefix, final String namespaceURI, final String localName,
            final String value) throws XMLStreamException {
        delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(final String namespaceURI, final String localName, final String value)
            throws XMLStreamException {
        delegate.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
        delegate.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
        delegate.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(final String data) throws XMLStreamException {
        preMarkup();
        delegate.writeComment(data);
        haveMarkup();
    }

    @Override
    public void writeProcessingInstruction(final String target) throws XMLStreamException {
        preMarkup();
        delegate.writeProcessingInstruction(target);
        haveMarkup();
    }

    @Override
    public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
        preMarkup();
        delegate.writeProcessingInstruction(target, data);
        haveMarkup();
    }

    @Override
    public void writeCData(final String data) throws XMLStreamException {
        delegate.writeCData(data);
        haveData();
    }

    @Override
    public void writeDTD(final String dtd) throws XMLStreamException {
        preMarkup();
        delegate.writeDTD(dtd);
        haveMarkup();
    }

    @Override
    public void writeEntityRef(final String arg0) throws XMLStreamException {
        delegate.writeEntityRef(arg0);
        haveData();
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument();
        haveMarkup();
    }

    @Override
    public void writeStartDocument(final String version) throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument(version);
        haveMarkup();
    }

    @Override
    public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument(encoding, version);
        haveMarkup();
    }

    @Override
    public void writeCharacters(final String text) throws XMLStreamException {
        delegate.writeCharacters(text);
        haveData();
    }

    @Override
    public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
        delegate.writeCharacters(text, start, len);
        haveData();
    }

    @Override
    public String getPrefix(final String uri) throws XMLStreamException {
        return delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
        delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(final String uri) throws XMLStreamException {
        delegate.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
        delegate.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(final String name) {
        return delegate.getProperty(name);
    }

    private void haveData() {
        stack[depth] |= HAVE_DATA;
    }

    private void haveMarkup() {
        stack[depth] |= HAVE_MARKUP;
    }

    private void preElement() throws XMLStreamException {
        preMarkup();

        // reallocate
        final var next = depth + 1;
        if (stack.length == next) {
            // FIXME: smarter sizing when we get to larger sizes
            final var newStack = new int[next + 2];
            System.arraycopy(stack, 0, newStack, 0, next);
            stack = newStack;
        }
        stack[next] = 0;
    }

    private void postElement() {
        // FIXME: called after we emit a new element
    }

    private void preMarkup() throws XMLStreamException {
        final int status = stack[depth];
        if ((status & HAVE_DATA) == 0 && (depth > 0 || status != 0)) {
            writeIndent(depth);
            haveMarkup();
        }
    }

    private void writeIndent(final int indentDepth) throws XMLStreamException {
        int toWrite = indentDepth * depth;
        if (toWrite < INDENT_CAPACITY) {
            delegate.writeCharacters(INDENT_CHARS, 0, 1 + toWrite);
            return;
        }

        // FIXME: finish this
        throw new UnsupportedOperationException();
    }
}
