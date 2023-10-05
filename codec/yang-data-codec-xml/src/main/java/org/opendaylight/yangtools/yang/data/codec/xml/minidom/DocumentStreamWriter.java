/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link XMLStreamWriter} which results in an immutable {@link Document}.
 */
public final class DocumentStreamWriter implements XMLStreamWriter {
    private abstract static class State {


    }

    private abstract static class ContainerState extends State {

        abstract void appendElement(Element element);
    }

    private static final class DocumentState extends State {
        final Document.Builder builder = Document.builder();

    }

    private static final class ElementState extends State {
        private final String namespace;
        private final String localName;
        private final String prefix;

        ElementState(final String namespace, final String localName, final String prefix) {
            this.namespace = namespace;
            this.localName = requireNonNull(localName);
            this.prefix = prefix;
        }
    }

    private final Deque<State> builders = new ArrayDeque<>();

    private Document document;

    @Override
    public void writeStartElement(final String localName) throws XMLStreamException {
        startElement(null, localName, null);

    }

    @Override
    public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
        startElement(requireNonNull(namespaceURI), localName, null);
    }

    @Override
    public void writeStartElement(final String prefix, final String localName, final String namespaceURI)
            throws XMLStreamException {
        startElement(requireNonNull(namespaceURI), localName, requireNonNull(prefix));
    }

    private void startElement(final String namespaceURI, final String localName, final String prefix)
            throws XMLStreamException {
        checkOpenContainer();
        builders.push(new ElementState(namespaceURI, localName, prefix));
    }

    @Override
    public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
        emptyElement(requireNonNull(namespaceURI), localName, null);
    }

    @Override
    public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI)
            throws XMLStreamException {
        emptyElement(requireNonNull(namespaceURI), localName, requireNonNull(prefix));
    }

    @Override
    public void writeEmptyElement(final String localName) throws XMLStreamException {
        emptyElement(null, localName, null);
    }

    private void emptyElement(final String namespaceURI, final String localName, final String prefix)
            throws XMLStreamException {
        checkOpenContainer().appendElement(new ImmutableContainerElement(namespaceURI, requireNonNull(localName),
            List.of(), List.of()));
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        if (builders.poll() instanceof DocumentState state) {
            document = state.builder.build();
        } else {
            throw new XMLStreamException("Unexpected end document");
        }
    }

    @Override
    public void writeAttribute(final String localName, final String value) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeAttribute(final String prefix, final String namespaceURI, final String localName,
            final String value) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeAttribute(final String namespaceURI, final String localName, final String value)
            throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeComment(final String data) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeProcessingInstruction(final String target) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCData(final String data) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDTD(final String dtd) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEntityRef(final String name) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument(final String version) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCharacters(final String text) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPrefix(final String uri) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDefaultNamespace(final String uri) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public NamespaceContext getNamespaceContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProperty(final String name) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    private @NonNull ContainerState checkOpenContainer() throws XMLStreamException {
        final var current = builders.peek();
        if (current instanceof ContainerState container) {
            return container;
        }
        throw new XMLStreamException("Unexpected state " + current);
    }
}
