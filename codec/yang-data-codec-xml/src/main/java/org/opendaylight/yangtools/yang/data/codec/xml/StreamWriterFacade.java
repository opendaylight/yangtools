/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The sole implementation of {@link ValueWriter}, tasked with synchronizing access to XMLStreamWriter state. The only
 * class referencing this class should be {@link XMLStreamNormalizedNodeStreamWriter}.
 */
final class StreamWriterFacade extends ValueWriter {
    private static final Logger LOG = LoggerFactory.getLogger(StreamWriterFacade.class);
    private static final Set<String> BROKEN_NAMESPACES = ConcurrentHashMap.newKeySet();
    private static final Set<String> LEGACY_ATTRIBUTES = ConcurrentHashMap.newKeySet();

    private final XMLStreamWriter writer;
    private final NamespacePrefixes prefixes;

    // QName of an element we delayed emitting. This only happens if it is a naked element, without any attributes,
    // namespace declarations or value.
    private QName openElement;

    StreamWriterFacade(final XMLStreamWriter writer, final @Nullable PreferredPrefixes pref) {
        this.writer = requireNonNull(writer);
        prefixes = new NamespacePrefixes(writer.getNamespaceContext(), pref);
    }

    void writeCharacters(final String text) throws XMLStreamException {
        if (!Strings.isNullOrEmpty(text)) {
            flushElement();
            writer.writeCharacters(text);
        }
    }

    @Override
    void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
        flushElement();
        writer.writeNamespace(prefix, namespaceURI);
    }

    @Override
    void writeAttribute(final String localName, final String value) throws XMLStreamException {
        flushElement();
        writer.writeAttribute(localName, value);
    }

    @Override
    void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value)
            throws XMLStreamException {
        flushElement();
        writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    NamespaceContext getNamespaceContext() {
        // Accessing namespace context is okay, because a delayed element is known to have no effect on the result
        return writer.getNamespaceContext();
    }

    private void flushElement() throws XMLStreamException {
        if (openElement != null) {
            writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, openElement.getLocalName(),
                openElement.getNamespace().toString());
            openElement = null;
        }
    }

    void writeStartElement(final QName qname) throws XMLStreamException {
        flushElement();

        final String namespace = qname.getNamespace().toString();
        final NamespaceContext context = writer.getNamespaceContext();
        final boolean reuseNamespace;
        if (context != null) {
            reuseNamespace = namespace.equals(context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
        } else {
            reuseNamespace = XMLConstants.DEFAULT_NS_PREFIX.equals(writer.getPrefix(namespace));
        }

        if (!reuseNamespace) {
            writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, qname.getLocalName(), namespace);
            writer.writeDefaultNamespace(namespace);
        } else {
            openElement = qname;
        }
    }

    void writeEndElement() throws XMLStreamException {
        if (openElement != null) {
            writer.writeEmptyElement(XMLConstants.DEFAULT_NS_PREFIX, openElement.getLocalName(),
                openElement.getNamespace().toString());
            openElement = null;
        } else {
            writer.writeEndElement();
        }
    }

    String getPrefix(final XMLNamespace uri, final String str) throws XMLStreamException {
        final String prefix = writer.getPrefix(str);
        if (prefix != null) {
            return prefix;
        }

        // This is needed to recover from attributes emitted while the namespace was not declared. Ordinarily
        // attribute namespaces would be bound in the writer, so the resulting XML is efficient, but we cannot rely
        // on that having been done.
        if (BROKEN_NAMESPACES.add(str)) {
            LOG.info("Namespace {} was not bound, please fix the caller", str, new Throwable());
        }

        return prefixes.encodePrefix(uri);
    }

    void close() throws XMLStreamException {
        // Mighty careful stepping here, we must end up closing the writer
        XMLStreamException failure = null;
        try {
            flushElement();
        } catch (XMLStreamException e) {
            failure = e;
            throw e;
        } finally {
            try {
                writer.close();
            } catch (XMLStreamException e) {
                if (failure != null) {
                    failure.addSuppressed(e);
                } else {
                    throw e;
                }
            }
        }
    }

    void flush() throws XMLStreamException {
        flushElement();
        writer.flush();
    }

    void anydataWriteStreamReader(final XMLStreamReader reader) throws XMLStreamException {
        flushElement();

        // Do not emit top-level element
        int depth = 0;
        while (reader.hasNext()) {
            final int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (depth != 0) {
                        forwardStartElement(reader);
                    } else {
                        // anydata: forward namespaces only, skipping the default namespace
                        for (int i = 0; i < reader.getNamespaceCount(); ++i) {
                            final String prefix = reader.getNamespacePrefix(i);
                            if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                                writer.writeNamespace(prefix, reader.getNamespaceURI(i));
                            }
                        }
                    }
                    ++depth;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    --depth;
                    if (depth != 0) {
                        writer.writeEndElement();
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.SPACE:
                    // Ignore comments and insignificant whitespace
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    // We are embedded: ignore start/end document events
                    break;
                case XMLStreamConstants.ATTRIBUTE:
                    forwardAttributes(reader);
                    break;
                case XMLStreamConstants.CDATA:
                    writer.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.NAMESPACE:
                    forwardNamespaces(reader);
                    break;
                case XMLStreamConstants.DTD:
                case XMLStreamConstants.NOTATION_DECLARATION:
                case XMLStreamConstants.ENTITY_DECLARATION:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                default:
                    throw new IllegalStateException("Unhandled event " + event);
            }
        }
    }

    void anyxmlWriteStreamReader(final DOMSourceXMLStreamReader reader) throws XMLStreamException {
        flushElement();

        // Do not emit top-level element
        int depth = 0;
        while (reader.hasNext()) {
            final int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (depth != 0) {
                        forwardStartElement(reader);
                    } else {
                        forwardNamespaces(reader);
                        // anyxml, hence we need to forward attributes
                        forwardAttributes(reader);
                    }
                    ++depth;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    --depth;
                    if (depth != 0) {
                        writer.writeEndElement();
                    }
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    forwardProcessingInstruction(reader);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.COMMENT:
                    writer.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.SPACE:
                    // Ignore insignificant whitespace
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    // We are embedded: ignore start/end document events
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    writer.writeEntityRef(reader.getLocalName());
                    break;
                case XMLStreamConstants.ATTRIBUTE:
                    forwardAttributes(reader);
                    break;
                case XMLStreamConstants.DTD:
                    writer.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    writer.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.NAMESPACE:
                    forwardNamespaces(reader);
                    break;
                case XMLStreamConstants.NOTATION_DECLARATION:
                case XMLStreamConstants.ENTITY_DECLARATION:
                default:
                    throw new IllegalStateException("Unhandled event " + event);
            }
        }
    }

    void emitNormalizedAnydata(final NormalizedAnydata anydata) throws XMLStreamException {
        flushElement();

        // Adjust state to point to parent node and ensure it can handle data tree nodes
        final SchemaInferenceStack.Inference inference;
        try {
            final SchemaInferenceStack stack = SchemaInferenceStack.ofInference(anydata.getInference());
            stack.exitToDataTree();
            inference = stack.toInference();
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            throw new XMLStreamException("Cannot emit " + anydata, e);
        }

        try {
            anydata.writeTo(XMLStreamNormalizedNodeStreamWriter.create(writer, inference));
        } catch (IOException e) {
            throw new XMLStreamException("Failed to emit anydata " + anydata, e);
        }
    }

    static void warnLegacyAttribute(final String localName) {
        if (LEGACY_ATTRIBUTES.add(localName)) {
            LOG.info("Encountered annotation {} not bound to module. Please examine the call stack and fix this "
                    + "warning by defining a proper YANG annotation to cover it", localName,
                    new Throwable("Call stack"));
        }
    }

    private void forwardAttributes(final XMLStreamReader reader) throws XMLStreamException {
        for (int i = 0, count = reader.getAttributeCount(); i < count; ++i) {
            final String localName = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            final String prefix = reader.getAttributePrefix(i);
            if (prefix != null) {
                writer.writeAttribute(prefix, reader.getAttributeNamespace(i), localName, value);
            } else {
                writer.writeAttribute(localName, value);
            }
        }
    }

    private void forwardNamespaces(final XMLStreamReader reader) throws XMLStreamException {
        for (int i = 0; i < reader.getNamespaceCount(); ++i) {
            writer.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
    }

    private void forwardProcessingInstruction(final XMLStreamReader reader) throws XMLStreamException {
        final String target = reader.getPITarget();
        final String data = reader.getPIData();
        if (data != null) {
            writer.writeProcessingInstruction(target, data);
        } else {
            writer.writeProcessingInstruction(target);
        }
    }

    private void forwardStartElement(final XMLStreamReader reader) throws XMLStreamException {
        final String localName = reader.getLocalName();
        final String prefix = reader.getPrefix();
        if (prefix != null) {
            writer.writeStartElement(prefix, localName, reader.getNamespaceURI());
        } else {
            writer.writeStartElement(localName);
        }

        forwardNamespaces(reader);
        forwardAttributes(reader);
    }
}
