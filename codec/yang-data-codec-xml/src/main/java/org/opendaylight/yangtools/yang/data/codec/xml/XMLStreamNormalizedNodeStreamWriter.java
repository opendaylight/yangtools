/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.data.util.NormalizedNodeStreamWriterStack;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * A {@link NormalizedNodeStreamWriter} which translates the events into an {@link XMLStreamWriter}, resulting in an
 * RFC6020 XML encoding. There are 2 versions of this class, one that takes a SchemaContext and encodes values
 * appropriately according to the YANG schema. The other is schema-less and merely outputs values using toString. The
 * latter is intended for debugging where doesn't have a SchemaContext available and isn't meant for production use.
 *
 * <p>
 * Due to backwards compatibility reasons this writer recognizes RFC7952 metadata include keys QNames with empty URI
 * (as exposed via {@link XmlParserStream#LEGACY_ATTRIBUTE_NAMESPACE}) as their QNameModule. These indicate an
 * unqualified XML attribute and their value can be assumed to be a String. Furthermore, this extends to qualified
 * attributes, which uses the proper namespace, but will not bind to a proper module revision. This caveat will be
 * removed in a future version.
 */
public abstract sealed class XMLStreamNormalizedNodeStreamWriter<T>
        implements NormalizedNodeStreamWriter, MetadataExtension
        permits SchemaAwareXMLStreamNormalizedNodeStreamWriter, SchemalessXMLStreamNormalizedNodeStreamWriter {
    private static final Logger LOG = LoggerFactory.getLogger(XMLStreamNormalizedNodeStreamWriter.class);
    private static final Set<String> BROKEN_ATTRIBUTES = ConcurrentHashMap.newKeySet();

    private final @NonNull StreamWriterFacade facade;

    XMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer, final @Nullable PreferredPrefixes pref) {
        facade = new StreamWriterFacade(writer, pref);
    }

    /**
     * Create a new writer with the specified context as its root.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context) {
        return create(writer, context, false);
    }

    /**
     * Create a new writer with the specified context as its root.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context, final boolean preferPrefixes) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context,
            NormalizedNodeStreamWriterStack.of(context), preferPrefixes);
    }

    /**
     * Create a new writer with the specified context and rooted at the specified node.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param inference root node inference
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveStatementInference inference) {
        return create(writer, inference, false);
    }

    /**
     * Create a new writer with the specified context and rooted at the specified node.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param inference root node inference
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveStatementInference inference, final boolean preferPrefixes) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, inference.modelContext(),
            NormalizedNodeStreamWriterStack.of(inference), preferPrefixes);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param path path
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context, final @Nullable Absolute path) {
        return create(writer, context, path, false);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param path path
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context, final @Nullable Absolute path, final boolean preferPrefixes) {
        return path == null ? create(writer, context)
            : new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context,
                NormalizedNodeStreamWriterStack.of(context, path), preferPrefixes);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified {@link YangInstanceIdentifier}.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param path path
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context, final YangInstanceIdentifier path) {
        return create(writer, context, path, false);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified {@link YangInstanceIdentifier}.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param path path
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter create(final XMLStreamWriter writer,
            final EffectiveModelContext context, final YangInstanceIdentifier path, final boolean preferPrefixes) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context,
            NormalizedNodeStreamWriterStack.of(context, path), preferPrefixes);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified operation's input.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param operationPath Parent operation (RPC or action) path.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter forInputOf(final XMLStreamWriter writer,
            final EffectiveModelContext context, final Absolute operationPath) {
        return forInputOf(writer, context, operationPath, false);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified operation's input.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param operationPath Parent operation (RPC or action) path.
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter forInputOf(final XMLStreamWriter writer,
            final EffectiveModelContext context, final Absolute operationPath, final boolean preferPrefixes) {
        return forOperation(writer, context, operationPath,
            YangConstants.operationInputQName(operationPath.lastNodeIdentifier().getModule()), preferPrefixes);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified operation's output.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param operationPath Parent operation (RPC or action) path.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter forOutputOf(final XMLStreamWriter writer,
            final EffectiveModelContext context, final Absolute operationPath) {
        return forOutputOf(writer, context, operationPath, false);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified operation's output.
     *
     * @param writer Output {@link XMLStreamWriter}
     * @param context Associated {@link EffectiveModelContext}.
     * @param operationPath Parent operation (RPC or action) path.
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static @NonNull NormalizedNodeStreamWriter forOutputOf(final XMLStreamWriter writer,
            final EffectiveModelContext context, final Absolute operationPath, final boolean preferPrefixes) {
        return forOperation(writer, context, operationPath,
            YangConstants.operationOutputQName(operationPath.lastNodeIdentifier().getModule()),
            preferPrefixes);
    }

    private static @NonNull NormalizedNodeStreamWriter forOperation(final XMLStreamWriter writer,
            final EffectiveModelContext context, final Absolute operationPath, final QName qname,
            final boolean preferPrefixes) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context,
            NormalizedNodeStreamWriterStack.ofOperation(context, operationPath, qname), preferPrefixes);
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
    public final List<MetadataExtension> supportedExtensions() {
        return List.of(this);
    }

    abstract void startAnydata(NodeIdentifier name);

    abstract void startList(NodeIdentifier name);

    abstract void startListItem(PathArgument name) throws IOException;

    abstract String encodeAnnotationValue(@NonNull ValueWriter xmlWriter, @NonNull QName qname, @NonNull Object value)
            throws XMLStreamException;

    abstract String encodeValue(@NonNull ValueWriter xmlWriter, @NonNull Object value, T context)
            throws XMLStreamException;

    final void writeValue(final @NonNull Object value, final T context) throws IOException {
        try {
            facade.writeCharacters(encodeValue(facade, value, context));
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

    final void anydataValue(final Object value) throws IOException {
        if (value instanceof DOMSourceAnydata) {
            try {
                facade.anydataWriteStreamReader(((DOMSourceAnydata) value).toStreamReader());
            } catch (XMLStreamException e) {
                throw new IOException("Unable to transform anydata value: " + value, e);
            }
        } else if (value instanceof NormalizedAnydata) {
            try {
                facade.emitNormalizedAnydata((NormalizedAnydata) value);
            } catch (XMLStreamException e) {
                throw new IOException("Unable to emit anydata value: " + value, e);
            }
        } else {
            throw new IllegalStateException("Unexpected anydata value " + value);
        }
    }

    final void anyxmlValue(final DOMSource domSource) throws IOException {
        if (domSource != null) {
            final Node domNode = requireNonNull(domSource.getNode());
            try {
                facade.anyxmlWriteStreamReader(new DOMSourceXMLStreamReader(domSource));
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
        for (final Entry<QName, Object> entry : attributes.entrySet()) {
            final QName qname = entry.getKey();
            final String namespace = qname.getNamespace().toString();
            final String localName = qname.getLocalName();
            final Object value = entry.getValue();

            // FIXME: remove this handling once we have complete mapping to metadata
            try {
                if (namespace.isEmpty()) {
                    // Legacy attribute, which is expected to be a String
                    StreamWriterFacade.warnLegacyAttribute(localName);
                    if (!(value instanceof String)) {
                        if (BROKEN_ATTRIBUTES.add(localName)) {
                            LOG.warn("""
                                Unbound annotation {} does not have a String value, ignoring it. Please fix the \
                                source of this annotation either by formatting it to a String or removing its \
                                use""", localName, new Throwable("Call stack"));
                        }
                        LOG.debug("Ignoring annotation {} value {}", localName, value);
                    } else {
                        facade.writeAttribute(localName, (String) value);
                        continue;
                    }
                } else {
                    final String prefix = facade.getPrefix(qname.getNamespace(), namespace);
                    final String attrValue = encodeAnnotationValue(facade, qname, value);
                    facade.writeAttribute(prefix, namespace, localName, attrValue);
                }
            } catch (final XMLStreamException e) {
                throw new IOException("Unable to emit attribute " + qname, e);
            }
        }
    }

    @Override
    public final boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (DOMSourceAnydata.class.isAssignableFrom(objectModel)
                || NormalizedAnydata.class.isAssignableFrom(objectModel)) {
            startAnydata(name);
            startElement(name.getNodeType());
            return true;
        }
        return false;
    }
}
