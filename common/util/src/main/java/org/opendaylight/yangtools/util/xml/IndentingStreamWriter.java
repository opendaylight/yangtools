/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link XMLStreamWriter} which emits indentation to a delegate.using an {@link IndentedXML} instance.
 */
final class IndentingStreamWriter implements XMLStreamWriter {
    private static final byte CONTENT = 1;
    private static final byte MARKUP  = 2;

    /**
     * Maximum stack depth allowed by {@link #MAX_NESTED_ELEMENTS}.
     */
    // FIXME: we only need this constant because we keep current state on stack. We should keep it in a field:
    //
    //           private byte current;
    //
    //        and push/pop when appropriate. That should improve performance by removing a memory access indirection
    //        to a shifting location.
    private static final int MAX_STACK_DEPTH = IndentedXML.MAX_NESTED_ELEMENTS + 1;

    // Backend writer. We keep a reference to it even if we are closed to keep things simple for methods that do not
    // throw XMLStreamException
    private final XMLStreamWriter delegate;

    // TODO: we probably want to encapsulate at least stack + depth into a separate class: this would allow us to define
    //       stack operations and simplify the code here.

    // The IndentedXML instance to handle indentation. Released on closed().
    private IndentedXML indent;
    // A stack holding two significant bits for each level. We start off small and grow as needed. Released on close().
    private byte[] stack = new byte[16];
    // Current stack depth, which to say now may open elements we have
    private int depth;

    @NonNullByDefault
    IndentingStreamWriter(final IndentedXML indent, final XMLStreamWriter delegate) {
        this.indent = requireNonNull(indent);
        this.delegate = requireNonNull(delegate);
    }

    // Pass-through delegated

    @Override
    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    @Override
    public void close() throws XMLStreamException {
        // Order of operations is somewhat important: we want delegate to handle events as closed before we free our
        // state. Not that we pretend any inter-thread safefy, but if we ever do, this is where things need to change.
        delegate.close();
        stack = null;
        indent = null;
    }

    @Override
    public Object getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        delegate.setDefaultNamespace(uri);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        delegate.setNamespaceContext(context);
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        delegate.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        delegate.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        delegate.writeNamespace(prefix, namespaceURI);
    }

    // Document handling is somewhat special

    @Override
    public void writeStartDocument() throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument();
        haveMarkup();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument(version);
        haveMarkup();
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        preMarkup();
        delegate.writeStartDocument(encoding, version);
        haveMarkup();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        // We need to close any open elements ourselves so as to have close tags correctly indented
        while (depth > 0) {
            writeEndElement();
        }
        delegate.writeEndDocument();

        if (stack[0] == CONTENT) {
            // We have emitted some data, but markup or indentation: add a newline
            delegate.writeCharacters(IndentedXML.NEWLINE);
        }
        stack[0] = 0;
    }

    // Self-contained markup: empty element, DTD, comment, PI

    @Override
    public void writeComment(String data) throws XMLStreamException {
        preMarkup();
        delegate.writeComment(data);
        haveMarkup();
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        preMarkup();
        delegate.writeDTD(dtd);
        haveMarkup();
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(localName);
        haveMarkup();
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(namespaceURI, localName);
        haveMarkup();
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        preMarkup();
        delegate.writeEmptyElement(prefix, localName, namespaceURI);
        haveMarkup();
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        preMarkup();
        delegate.writeProcessingInstruction(target);
        haveMarkup();
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        preMarkup();
        delegate.writeProcessingInstruction(target, data);
        haveMarkup();
    }

    // Data content

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        delegate.writeCharacters(text);
        haveContent();
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        delegate.writeCharacters(text, start, len);
        haveContent();
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        delegate.writeCData(data);
        haveContent();

    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        delegate.writeEntityRef(name);
        haveContent();
    }

    // start/end element, here is where we apply indentation

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        preStartElement();
        delegate.writeStartElement(localName);
        postStartElement();
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        preStartElement();
        delegate.writeStartElement(namespaceURI, localName);
        postStartElement();
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        preStartElement();
        delegate.writeStartElement(prefix, localName, namespaceURI);
        postStartElement();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (depth == 0) {
            throw new XMLStreamException("No element to end");
        }

        final int nextDepth = depth - 1;
        if (stack[depth] == MARKUP) {
            // we have written only markup, emit indentation before end element tag
            indent.writeIndent(delegate, nextDepth);
        }
        delegate.writeEndElement();
        depth = nextDepth;
    }

    private void haveContent() {
        stack[depth] |= CONTENT;
    }

    private void haveMarkup() {
        stack[depth] |= MARKUP;
    }

    private void preMarkup() {
        // FIXME: implement this
    }

    private void preStartElement() throws XMLStreamException {
        preMarkup();

        // set up stack[depth + 1] for use, growing the stack if needed
        final int nextDepth = depth + 1;
        if (stack.length == nextDepth) {
            growStack();
        } else {
            stack[nextDepth] = 0;
        }
    }

    private void postStartElement() {
        haveMarkup();
        depth++;
    }

    private void growStack() throws XMLStreamException {
        final var size = stack.length;
        if (size == MAX_STACK_DEPTH) {
            throw new XMLStreamException(
                "More than " + IndentedXML.MAX_NESTED_ELEMENTS + " nested elenents are not supported");
        }

        final var resized = new byte[Math.min(size << 1, MAX_STACK_DEPTH)];
        System.arraycopy(stack, 0, resized, 0, size);
        stack = resized;
    }

    @Override
    public String toString() {
        final var localIndent = indent;
        final var localStack = stack;

        final var sb = new StringBuilder("IndentingStreamWriter [delegate=").append(delegate).append(", ");
        if (localIndent != null && localStack != null) {
            sb.append(" indent=").append(localIndent).append(", depth=").append(depth);
        } else {
            sb.append("closed=true");
        }
        return sb.append(']').toString();
    }
}
