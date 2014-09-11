/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * This implementation will create JSON output as output stream.
 *
 * Values of leaf and leaf-list are NOT translated according to codecs.
 *
 * FIXME: rewrite this in terms of {@link JsonWriter}.
 */
public class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    /**
     * RFC6020 deviation: we are not required to emit empty containers unless they
     * are marked as 'presence'.
     */
    private static final boolean DEFAULT_EMIT_EMPTY_CONTAINERS = true;
    private static final Collection<Class<?>> NUMERIC_CLASSES =
            ImmutableSet.<Class<?>>of(Byte.class, Short.class, Integer.class, Long.class, BigInteger.class, BigDecimal.class);

    private final SchemaContext schemaContext;
    private final SchemaTracker tracker;
    private final CodecFactory codecs;
    private final Writer writer;
    private final String indent;
    private JSONStreamWriterContext context;

    private JSONNormalizedNodeStreamWriter(final SchemaContext schemaContext,
            final Writer writer, final int indentSize) {
        this(schemaContext, SchemaPath.ROOT, writer, null, indentSize);
    }

    private JSONNormalizedNodeStreamWriter(final SchemaContext schemaContext, final SchemaPath path,
            final Writer writer, final URI initialNs, final int indentSize) {
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.writer = Preconditions.checkNotNull(writer);

        Preconditions.checkArgument(indentSize >= 0, "Indent size must be non-negative");
        if (indentSize != 0) {
            indent = Strings.repeat(" ", indentSize);
        } else {
            indent = null;
        }
        this.codecs = CodecFactory.create(schemaContext);
        this.tracker = SchemaTracker.create(schemaContext, path);
        this.context = new JSONStreamWriterRootContext(initialNs);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param schemaContext Schema context
     * @param writer Output writer
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final SchemaContext schemaContext, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(schemaContext, writer, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param schemaContext Schema context
     * @param path Root schemapath
     * @param writer Output writer
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final SchemaContext schemaContext, final SchemaPath path, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(schemaContext, path, writer, null, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param schemaContext Schema context
     * @param path Root schemapath
     * @param writer Output writer
     * @param initialNs Initial namespace
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final SchemaContext schemaContext, final SchemaPath path,
            final URI initialNs, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(schemaContext, path, writer, initialNs, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * @param schemaContext Schema context
     * @param writer Output writer
     * @param indentSize indentation size
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final SchemaContext schemaContext, final Writer writer, final int indentSize) {
        return new JSONNormalizedNodeStreamWriter(schemaContext, writer, indentSize);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        final Codec<Object, Object> codec = codecs.codecFor(schema.getType());

        context.emittingChild(schemaContext, writer, indent);
        context.writeChildJsonIdentifier(schemaContext, writer, name.getNodeType());
        writeValue(codec.serialize(value));
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        final Codec<Object, Object> codec = codecs.codecFor(schema.getType());

        context.emittingChild(schemaContext, writer, indent);
        writeValue(codec.serialize(value));
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final ContainerSchemaNode schema = tracker.startContainerNode(name);
        context = new JSONStreamWriterNamedObjectContext(context, name, schema.isPresenceContainer() || DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startListItem(name);
        context = new JSONStreamWriterObjectContext(context, name, DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        tracker.startListItem(identifier);
        context = new JSONStreamWriterObjectContext(context, identifier, DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
        context = new JSONStreamWriterInvisibleContext(context);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        tracker.startAugmentationNode(identifier);
        context = new JSONStreamWriterInvisibleContext(context);
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        // FIXME: should have a codec based on this :)

        context.emittingChild(schemaContext, writer, indent);
        context.writeChildJsonIdentifier(schemaContext, writer, name.getNodeType());
        writeValue(value);
    }

    @Override
    public void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(schemaContext, writer, indent);
    }

    private void writeValue(final Object value) throws IOException {
        final String str = String.valueOf(value);

        if (!NUMERIC_CLASSES.contains(value.getClass())) {
            writer.append('"');
            writer.append(str);
            writer.append('"');
        } else {
            writer.append(str);
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

}
