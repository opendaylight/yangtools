/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamAttributeWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A {@link NormalizedNodeStreamWriter} which translates the events into an {@link XMLStreamWriter},
 * resulting in a RFC 6020 XML encoding. There are 2 versions of this class, one that takes a
 * SchemaContext and encodes values appropriately according to the yang schema. The other is
 * schema-less and merely outputs values using toString. The latter is intended for debugging
 * where doesn't have a SchemaContext available and isn't meant for production use.
 */
public abstract class XMLStreamNormalizedNodeStreamWriter<T> implements NormalizedNodeStreamAttributeWriter {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final @NonNull StreamWriterFacade facade;

    XMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer) {
        facade = new StreamWriterFacade(writer);
    }

    /**
     * Create a new writer with the specified context as its root.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link SchemaContext}.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final SchemaContext context) {
        return create(writer, context, context);
    }

    /**
     * Create a new writer with the specified context and rooted at the specified node.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link SchemaContext}.
     * @param rootNode Root node
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer, final SchemaContext context,
            final DataNodeContainer rootNode) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context, SchemaTracker.create(rootNode));
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link SchemaContext}.
     * @param path path
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer, final SchemaContext context,
            final SchemaPath path) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context, SchemaTracker.create(context, path));
    }

    /**
     * Create a new schema-less writer. Note that this version is intended for debugging
     * where doesn't have a SchemaContext available and isn't meant for production use.
     *
     * @param writer Output {@link XMLStreamWriter}
     *
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter createSchemaless(final XMLStreamWriter writer) {
        return new SchemalessXMLStreamNormalizedNodeStreamWriter(writer);
    }

    /**
     * Utility method for formatting an {@link Element} to a string.
     *
     * @deprecated This method not used anywhere, users are advised to use their own formatting.
     */
    @Deprecated
    public static String toString(final Element xml) {
        try {
            final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            final StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(new DOMSource(xml), result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerException e) {
            throw new IllegalStateException("Unable to serialize xml element " + xml, e);
        }
    }

    abstract void startList(NodeIdentifier name);

    abstract void startListItem(PathArgument name) throws IOException;

    abstract void writeValue(@NonNull ValueWriter xmlWriter, @NonNull Object value, T context)
            throws XMLStreamException;

    final void writeValue(final @NonNull Object value, final T context) throws IOException {
        try {
            writeValue(facade, value, context);
        } catch (XMLStreamException e) {
            throw new IOException("Failed to write value", e);
        }
    }

    final void startElement(final QName qname) throws IOException {
        try {
            facade.writeStartElement(qname);
        } catch (XMLStreamException e) {
            throw new IOException("Failed to start element", e);
        }
    }

    final void endElement() throws IOException {
        try {
            facade.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to end element", e);
        }
    }

    final void anyxmlValue(final Object value) throws IOException {
        if (value != null) {
            checkArgument(value instanceof DOMSource, "AnyXML value must be DOMSource, not %s", value);
            final DOMSource domSource = (DOMSource) value;
            final Node domNode = requireNonNull(domSource.getNode());

            try {
                facade.writeStreamReader(new DOMSourceXMLStreamReader(domSource));
            } catch (XMLStreamException e) {
                throw new IOException("Unable to transform anyXml value: " + domNode, e);
            }
        }
    }

    @Override
    public final void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startListItem(name);
    }

    @Override
    public final void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        startListItem(identifier);
    }

    @Override
    public final void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public final void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public final void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        startList(name);
    }

    @Override
    public final void close() throws IOException {
        try {
            facade.close();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to close writer", e);
        }
    }

    @Override
    public final void flush() throws IOException {
        try {
            facade.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to flush writer", e);
        }
    }

    @Override
    public final void attributes(final Map<QName, String> attributes) throws IOException {
        if (!attributes.isEmpty()) {
            try {
                facade.writeAttributes(attributes);
            } catch (final XMLStreamException e) {
                throw new IOException("Unable to emit attributes " + attributes, e);
            }
        }
    }
}
