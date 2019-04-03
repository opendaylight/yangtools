/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Node;

/**
 * A {@link NormalizedNodeStreamWriter} which translates the events into an {@link XMLStreamWriter},
 * resulting in a RFC 6020 XML encoding. There are 2 versions of this class, one that takes a
 * SchemaContext and encodes values appropriately according to the YANG schema. The other is
 * schema-less and merely outputs values using toString. The latter is intended for debugging
 * where doesn't have a SchemaContext available and isn't meant for production use.
 */
public abstract class XMLStreamNormalizedNodeStreamWriter<T> implements NormalizedNodeStreamWriter,
        NormalizedMetadataStreamWriter {
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

    @Override
    public final ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(NormalizedMetadataStreamWriter.class, this);
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

    final void anyxmlValue(final DOMSource domSource) throws IOException {
        if (domSource != null) {
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
    public final void metadata(final ImmutableMap<QName, Object> attributes) throws IOException {
        if (!attributes.isEmpty()) {
            try {
                facade.writeAttributes(attributes);
            } catch (final XMLStreamException e) {
                throw new IOException("Unable to emit attributes " + attributes, e);
            }
        }
    }
}
