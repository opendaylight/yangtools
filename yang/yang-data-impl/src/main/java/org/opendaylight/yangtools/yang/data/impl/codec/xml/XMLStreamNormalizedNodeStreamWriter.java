/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

import com.google.common.base.Preconditions;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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

/**
 * A {@link NormalizedNodeStreamWriter} which translates the events into an
 * {@link XMLStreamWriter}, resulting in a RFC 6020 XML encoding.
 */
public final class XMLStreamNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final XmlStreamUtils UTILS = XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER);

    private final XMLStreamWriter writer;
    private final SchemaTracker tracker;

    private XMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer, final SchemaContext context, final SchemaPath path) {
        this.writer = Preconditions.checkNotNull(writer);
        this.tracker = SchemaTracker.create(context, path);
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

    private void writeStartElement( QName qname) throws XMLStreamException {
        String ns = qname.getNamespace().toString();
        String parentNs = writer.getNamespaceContext().getNamespaceURI(DEFAULT_NS_PREFIX);
        writer.writeStartElement(DEFAULT_NS_PREFIX, qname.getLocalName(), ns);
        if (!ns.equals(parentNs)) {
            writer.writeDefaultNamespace(ns);
        }
    }

    private void writeElement(final QName qname, final TypeDefinition<?> type, final Object value) throws IOException {
        try {
            writeStartElement(qname);
            if (value != null) {
                UTILS.writeValue(writer, type, value);
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

        writeElement(schema.getQName(), schema.getType(), value);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        writeElement(schema.getQName(), schema.getType(), value);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
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
        final QName qname = schema.getQName();
        try {
            writeStartElement(qname);
            if (value != null) {
                UTILS.writeValue(writer, (Node<?>)value, schema);
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to emit element", e);
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
}
