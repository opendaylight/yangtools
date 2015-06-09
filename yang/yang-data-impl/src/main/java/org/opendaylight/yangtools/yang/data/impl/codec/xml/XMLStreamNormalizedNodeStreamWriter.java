/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stream.StreamResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamAttributeWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Element;

/**
 * A {@link NormalizedNodeStreamWriter} which translates the events into an
 * {@link XMLStreamWriter}, resulting in a RFC 6020 XML encoding.
 */
public final class XMLStreamNormalizedNodeStreamWriter implements NormalizedNodeStreamAttributeWriter {

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final XMLStreamWriter writer;
    private final SchemaTracker tracker;
    private final XmlStreamUtils streamUtils;
    private final RandomPrefix randomPrefix;

    private XMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer, final SchemaContext context, final SchemaPath path) {
        this.writer = Preconditions.checkNotNull(writer);
        this.tracker = SchemaTracker.create(context, path);
        this.streamUtils = XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER, context);
        randomPrefix = new RandomPrefix();
    }

    /**
     * Create a new writer with the specified context as its root.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link SchemaContext}.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static NormalizedNodeStreamWriter create(final XMLStreamWriter writer, final SchemaContext context) {
        return create( writer, context, SchemaPath.ROOT);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link SchemaContext}.
     *
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static NormalizedNodeStreamWriter create(final XMLStreamWriter writer, final SchemaContext context, final SchemaPath path) {
        return new XMLStreamNormalizedNodeStreamWriter(writer, context, path);
    }

    private void writeStartElement(final QName qname) throws XMLStreamException {
        String ns = qname.getNamespace().toString();
        writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, qname.getLocalName(), ns);
        if(writer.getNamespaceContext() != null) {
            String parentNs = writer.getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
            if (!ns.equals(parentNs)) {
                writer.writeDefaultNamespace(ns);
            }
        }
    }

    private void writeElement(final QName qname, final TypeDefinition<?> type, final Object value) throws IOException {
        try {
            writeStartElement(qname);
            if (value != null) {
                streamUtils.writeValue(writer, type, value, qname.getModule());
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to emit element", e);
        }
    }

    private void writeElement(final QName qname, final SchemaNode schemaNode, final Object value) throws IOException {
        try {
            writeStartElement(qname);
            if (value != null) {
                streamUtils.writeValue(writer, schemaNode, value, qname.getModule());
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to emit element", e);
        }
    }

    private void writeElement(final QName qname, final SchemaNode schemaNode, final Object value, final Map<QName, String> attributes) throws IOException {
        try {
            writeStartElement(qname);

            writeAttributes(attributes);
            if (value != null) {
                streamUtils.writeValue(writer, schemaNode, value, qname.getModule());
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to emit element", e);
        }
    }

    private void startElement(final QName qname) throws IOException {
        try {
            writeStartElement(qname);
        } catch (XMLStreamException e) {
            throw new IOException("Failed to start element", e);
        }
    }

    private void startList(final NodeIdentifier name) {
        tracker.startList(name);
    }

    private void startListItem(final PathArgument name) throws IOException {
        tracker.startListItem(name);
        startElement(name.getNodeType());
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        writeElement(schema.getQName(), schema, value);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value, final Map<QName, String> attributes) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        writeElement(schema.getQName(), schema, value, attributes);
    }

    @Override
    public void leafSetEntryNode(final Object value, final Map<QName, String> attributes) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        writeElement(schema.getQName(), schema, value, attributes);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint, final Map<QName, String> attributes) throws IOException {
        startContainerNode(name, childSizeHint);
        writeAttributes(attributes);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint, final Map<QName, String> attributes) throws IOException {
        startUnkeyedListItem(name, childSizeHint);
        writeAttributes(attributes);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint, final Map<QName, String> attributes) throws IOException {
        startMapEntryNode(identifier, childSizeHint);
        writeAttributes(attributes);
    }

    private void writeAttributes(final Map<QName, String> attributes) throws IOException {
        for (final Map.Entry<QName, String> qNameStringEntry : attributes.entrySet()) {
            try {
                final String namespace = qNameStringEntry.getKey().getNamespace().toString();

                if(Strings.isNullOrEmpty(namespace)) {
                    writer.writeAttribute(qNameStringEntry.getKey().getLocalName(), qNameStringEntry.getValue());
                } else {
                    final String prefix = randomPrefix.encodePrefix(qNameStringEntry.getKey().getNamespace());
                    writer.writeAttribute(prefix, namespace, qNameStringEntry.getKey().getLocalName(), qNameStringEntry.getValue());
                }
            } catch (final XMLStreamException e) {
                throw new IOException("Unable to emit attribute " + qNameStringEntry, e);
            }
        }
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        writeElement(schema.getQName(), schema, value);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);
        startElement(schema.getQName());
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startListItem(name);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) throws IOException {
        startListItem(identifier);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        tracker.startAugmentationNode(identifier);
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        if (value != null) {
            Preconditions.checkArgument(value instanceof DOMSource, "AnyXML value must be DOMSource, not %s", value);
            final QName qname = schema.getQName();
            final DOMSource domSource = (DOMSource) value;
            Preconditions.checkNotNull(domSource.getNode());
            Preconditions.checkArgument(domSource.getNode().getNodeName().equals(qname.getLocalName()));
            Preconditions.checkArgument(domSource.getNode().getNamespaceURI().equals(qname.getNamespace().toString()));
            try {
                // TODO can the transformer be a constant ? is it thread safe ?
                final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
                // Writer has to be wrapped in a wrapper that ignores endDocument event
                // EndDocument event forbids any other modification to the writer so a nested anyXml breaks serialization
                transformer.transform(domSource, new StAXResult(new DelegateWriterNoEndDoc(writer)));
            } catch (final TransformerException e) {
                throw new IOException("Unable to transform anyXml(" + name + ") value: " + value, e);
            }
        }
    }

    public static String toString(final Element xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }

    @Override
    public void endNode() throws IOException {
        final Object schema = tracker.endNode();

        try {
            if (schema instanceof ListSchemaNode) {
                // For lists, we only emit end element on the inner frame
                final Object parent = tracker.getParent();
                if (parent == schema) {
                    writer.writeEndElement();
                }
            } else if (schema instanceof ContainerSchemaNode) {
                // Emit container end element
                writer.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new IOException("Failed to end element", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to close writer", e);
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to flush writer", e);
        }
    }

    /**
     * Delegate writer that ignores writeEndDocument event. Used for AnyXml serialization.
     */
    private static final class DelegateWriterNoEndDoc implements XMLStreamWriter {
        private final XMLStreamWriter writer;

        public DelegateWriterNoEndDoc(final XMLStreamWriter writer) {
            this.writer = writer;
        }

        @Override
        public void writeStartElement(final String localName) throws XMLStreamException {
            writer.writeStartElement(localName);
        }

        @Override
        public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
            writer.writeStartElement(namespaceURI, localName);
        }

        @Override
        public void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
            writer.writeStartElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
            writer.writeEmptyElement(namespaceURI, localName);
        }

        @Override
        public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
            writer.writeEmptyElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(final String localName) throws XMLStreamException {
            writer.writeEmptyElement(localName);
        }

        @Override
        public void writeEndElement() throws XMLStreamException {
            writer.writeEndElement();

        }

        @Override
        public void writeEndDocument() throws XMLStreamException {
            // End document is disabled
        }

        @Override
        public void close() throws XMLStreamException {
            writer.close();
        }

        @Override
        public void flush() throws XMLStreamException {
            writer.flush();
        }

        @Override
        public void writeAttribute(final String localName, final String value) throws XMLStreamException {
            writer.writeAttribute(localName, value);
        }

        @Override
        public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        }

        @Override
        public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
            writer.writeAttribute(namespaceURI, localName, value);
        }

        @Override
        public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
            // Workaround for default namespace
            // If a namespace is not prefixed, it is is still treated as prefix namespace. This results in the NamespaceSupport class ignoring the namespace since xmlns is not a valid prefix
            // Write the namespace at least as an attribute
            // TODO this is a hotfix, the transformer itself should write namespaces passing the namespace in writeStartElement method
            if (prefix.equals("xml") || prefix.equals("xmlns")) {
                writer.writeAttribute(prefix, namespaceURI);
            } else {
                writer.writeNamespace(prefix, namespaceURI);
            }
        }

        @Override
        public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
            writer.writeDefaultNamespace(namespaceURI);
        }

        @Override
        public void writeComment(final String data) throws XMLStreamException {
            writer.writeComment(data);
        }

        @Override
        public void writeProcessingInstruction(final String target) throws XMLStreamException {
            writer.writeProcessingInstruction(target);
        }

        @Override
        public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
            writer.writeProcessingInstruction(target, data);
        }

        @Override
        public void writeCData(final String data) throws XMLStreamException {
            writer.writeCData(data);
        }

        @Override
        public void writeDTD(final String dtd) throws XMLStreamException {
            writer.writeDTD(dtd);
        }

        @Override
        public void writeEntityRef(final String name) throws XMLStreamException {
            writer.writeEntityRef(name);
        }

        @Override
        public void writeStartDocument() throws XMLStreamException {
        }

        @Override
        public void writeStartDocument(final String version) throws XMLStreamException {
        }

        @Override
        public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        }

        @Override
        public void writeCharacters(final String text) throws XMLStreamException {
            writer.writeCharacters(text);
        }

        @Override
        public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
            writer.writeCharacters(text, start, len);
        }

        @Override
        public String getPrefix(final String uri) throws XMLStreamException {
            return writer.getPrefix(uri);
        }

        @Override
        public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
            // Disabled since it causes exceptions in the underlying writer
        }

        @Override
        public void setDefaultNamespace(final String uri) throws XMLStreamException {
            writer.setDefaultNamespace(uri);
        }

        @Override
        public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
            writer.setNamespaceContext(context);
        }

        @Override
        public NamespaceContext getNamespaceContext() {
            return writer.getNamespaceContext();
        }

        @Override
        public Object getProperty(final String name) {
            return writer.getProperty(name);
        }
    }
}
