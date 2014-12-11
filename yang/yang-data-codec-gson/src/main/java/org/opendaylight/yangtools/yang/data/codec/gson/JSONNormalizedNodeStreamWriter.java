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

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * This implementation will create JSON output as output stream.
 *
 * Values of leaf and leaf-list are NOT translated according to codecs.
 *
 */
public class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    /**
     * RFC6020 deviation: we are not required to emit empty containers unless they
     * are marked as 'presence'.
     */
    private static final boolean DEFAULT_EMIT_EMPTY_CONTAINERS = true;

    private final SchemaTracker tracker;
    private final JSONCodecFactory codecs;
    private final JSONWriter writer;
    private JSONStreamWriterContext context;

    private JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory, final SchemaPath path,
            final Writer writer, final JSONWriterFactory.JSONWriterType jsonWriterType, final URI initialNs, final int indentSize) {

        this.writer = JSONWriterFactory.createJSONWriter(writer,jsonWriterType);

        Preconditions.checkArgument(indentSize >= 0, "Indent size must be non-negative");
        String indent = "";
        if (indentSize != 0) indent = Strings.repeat(" ", indentSize);

        this.writer.setIndent(indent);

        this.codecs = Preconditions.checkNotNull(codecFactory);
        this.tracker = SchemaTracker.create(codecFactory.getSchemaContext(), path);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, JSONWriterFactory.JSONWriterType.DEFAULT_JSON_WRITER , null, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, JSONWriterFactory.JSONWriterType.DEFAULT_JSON_WRITER, null, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, JSONWriterFactory.JSONWriterType.DEFAULT_JSON_WRITER, initialNs, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, JSONWriterFactory.JSONWriterType.DEFAULT_JSON_WRITER, null, indentSize);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream. The codec factory
     * can be reused between multiple writers.
     *
     * @param codecFactory JSON codec factory
     * @param writer Output writer
     * @param indentSize indentation size
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONCodecFactory codecFactory, final Writer writer, final int indentSize) {
        return new JSONNormalizedNodeStreamWriter(codecFactory, SchemaPath.ROOT, writer, JSONWriterFactory.JSONWriterType.DEFAULT_JSON_WRITER, null, indentSize);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param jsonWriterType JSONWriter type (e.g. JSONWriterFactory.JSONWriterType.GOOGLE_JSON_WRITER)
     * @param schemaContext Schema context
     * @param writer Output writer
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONWriterFactory.JSONWriterType jsonWriterType, final SchemaContext schemaContext, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, jsonWriterType , null, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param jsonWriterType JSONWriter type (e.g. JSONWriterFactory.JSONWriterType.GOOGLE_JSON_WRITER)
     * @param schemaContext Schema context
     * @param path Root schemapath
     * @param writer Output writer
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONWriterFactory.JSONWriterType jsonWriterType, final SchemaContext schemaContext, final SchemaPath path, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, jsonWriterType, null, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified {@link Writer}.
     *
     * @param jsonWriterType JSONWriter type (e.g. JSONWriterFactory.JSONWriterType.GOOGLE_JSON_WRITER)
     * @param schemaContext Schema context
     * @param path Root schemapath
     * @param writer Output writer
     * @param initialNs Initial namespace
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONWriterFactory.JSONWriterType jsonWriterType, final SchemaContext schemaContext, final SchemaPath path,
            final URI initialNs, final Writer writer) {
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, jsonWriterType, initialNs, 0);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * @param jsonWriterType JSONWriter type (e.g. JSONWriterFactory.JSONWriterType.GOOGLE_JSON_WRITER)
     * @param schemaContext Schema context
     * @param writer Output writer
     * @param indentSize indentation size
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONWriterFactory.JSONWriterType jsonWriterType, final SchemaContext schemaContext, final Writer writer, final int indentSize) {
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, jsonWriterType, null, indentSize);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream. The codec factory
     * can be reused between multiple writers.
     *
     * @param jsonWriterType JSONWriter type (e.g. JSONWriterFactory.JSONWriterType.GOOGLE_JSON_WRITER)
     * @param codecFactory JSON codec factory
     * @param writer Output writer
     * @param indentSize indentation size
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter create(final JSONWriterFactory.JSONWriterType jsonWriterType, final JSONCodecFactory codecFactory, final Writer writer, final int indentSize) {
        return new JSONNormalizedNodeStreamWriter(codecFactory, SchemaPath.ROOT, writer, jsonWriterType, null, indentSize);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        final JSONCodec<Object> codec = codecs.codecFor(schema.getType());

        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());

        writeValue(value, codec);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        final JSONCodec<Object> codec = codecs.codecFor(schema.getType());

        context.emittingChild(codecs.getSchemaContext(), writer);

        writeValue(value, codec);
    }

    /*
     * Warning suppressed due to static final constant which triggers a warning
     * for the call to schema.isPresenceContainer().
     */
    @SuppressWarnings("unused")
    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final ContainerSchemaNode schema = tracker.startContainerNode(name);
        context = new JSONStreamWriterNamedObjectContext(context, name, DEFAULT_EMIT_EMPTY_CONTAINERS || schema.isPresenceContainer());
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
        @SuppressWarnings("unused")
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        // FIXME: should have a codec based on this :)

        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
        writer.value(String.valueOf(value));
    }

    @Override
    public void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(codecs.getSchemaContext(), writer);
        if(context instanceof JSONStreamWriterRootContext) context.emitEnd(writer);
    }

    private void writeValue(Object value, JSONCodec<Object> codec)
            throws IOException {

        String codecReturnValue = codec.serialize(value);

        Number numberValue = null;
        if (!codec.needQuotes() && value instanceof Number
                && (numberValue = getNumber(codecReturnValue)) != null)
            writer.value(numberValue);
        else if (!codec.needQuotes() && value instanceof Boolean
                && (codecReturnValue.equalsIgnoreCase("true") || codecReturnValue
                        .equalsIgnoreCase("false")))
            writer.value(Boolean.valueOf(codecReturnValue));
        else
            writer.value(codecReturnValue);

    }

    private Number getNumber(String codecReturnValue) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);

        Number numberValue = formatter.parse(codecReturnValue, pos);

        if (codecReturnValue.length() == pos.getIndex())
            return numberValue;
        else
            return null;
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
