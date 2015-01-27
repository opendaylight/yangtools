/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
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
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
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

    /**
     * Matcher used to check if a string needs to be escaped.
     */
    private static final CharMatcher JSON_ILLEGAL_STRING_CHARACTERS = CharMatcher.anyOf("\\\"\n\r");

    private final SchemaTracker tracker;
    private final JSONCodecFactory codecs;
    private final Writer writer;
    private final String indent;
    private JSONStreamWriterContext context;

    private JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory, final SchemaPath path,
            final Writer writer, final URI initialNs, final int indentSize) {
        this.writer = Preconditions.checkNotNull(writer);

        Preconditions.checkArgument(indentSize >= 0, "Indent size must be non-negative");
        if (indentSize != 0) {
            indent = Strings.repeat(" ", indentSize);
        } else {
            indent = null;
        }
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, null, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, null, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), path, writer, initialNs, 0);
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
        return new JSONNormalizedNodeStreamWriter(JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, writer, null, indentSize);
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
        return new JSONNormalizedNodeStreamWriter(codecFactory, SchemaPath.ROOT, writer, null, indentSize);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        final JSONCodec<Object> codec = codecs.codecFor(schema.getType());

        context.emittingChild(codecs.getSchemaContext(), writer, indent);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
        writeValue(codec.serialize(value), codec.needQuotes());
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

        context.emittingChild(codecs.getSchemaContext(), writer, indent);
        writeValue(codec.serialize(value), codec.needQuotes());
    }

    /*
     * Warning suppressed due to static final constant which triggers a warning
     * for the call to schema.isPresenceContainer().
     */
    @SuppressWarnings("unused")
    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);

        final boolean isPresence = schema instanceof ContainerSchemaNode ?
                ((ContainerSchemaNode) schema).isPresenceContainer() : DEFAULT_EMIT_EMPTY_CONTAINERS;

        context = new JSONStreamWriterNamedObjectContext(context, name, isPresence);
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

        context.emittingChild(codecs.getSchemaContext(), writer, indent);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
        writeValue(String.valueOf(value), true);
    }

    @Override
    public void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(codecs.getSchemaContext(), writer, indent);
    }

    private void writeValue(final String str, final boolean needQuotes) throws IOException {
        if (needQuotes) {
            writer.append('"');

            final int needEscape = JSON_ILLEGAL_STRING_CHARACTERS.countIn(str);
            if (needEscape != 0) {
                final char[] escaped = new char[str.length() + needEscape];
                int offset = 0;

                for (int i = 0; i < str.length(); i++) {
                    final char c = str.charAt(i);
                    if (JSON_ILLEGAL_STRING_CHARACTERS.matches(c)) {
                        escaped[offset++] = '\\';
                    }
                    escaped[offset++] = c;
                }
                writer.write(escaped);
            } else {
                writer.append(str);
            }

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
