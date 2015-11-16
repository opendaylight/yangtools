/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.URI;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * This implementation will create JSON output as output stream.
 *
 * Values of leaf and leaf-list are NOT translated according to codecs.
 *
 */
public final class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    /**
     * RFC6020 deviation: we are not required to emit empty containers unless they
     * are marked as 'presence'.
     */
    private static final boolean DEFAULT_EMIT_EMPTY_CONTAINERS = true;

    private final SchemaTracker tracker;
    private final JSONCodecFactory codecs;
    private final JsonWriter writer;
    private JSONStreamWriterContext context;

    private JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory, final SchemaPath path, final JsonWriter JsonWriter, final JSONStreamWriterRootContext rootContext) {
        this.writer = Preconditions.checkNotNull(JsonWriter);
        this.codecs = Preconditions.checkNotNull(codecFactory);
        this.tracker = SchemaTracker.create(codecFactory.getSchemaContext(), path);
        this.context = Preconditions.checkNotNull(rootContext);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * The codec factory can be reused between multiple writers.
     *
     * Returned writer is exclusive user of JsonWriter, which means it will start
     * top-level JSON element and ends it.
     *
     * This instance of writer can be used only to emit one top level element,
     * otherwise it will produce incorrect JSON.
     *
     * @param codecFactory JSON codec factory
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory, final SchemaPath path, final URI initialNs, final JsonWriter jsonWriter) {
        return new JSONNormalizedNodeStreamWriter(codecFactory, path, jsonWriter, new JSONStreamWriterExclusiveRootContext(initialNs));
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * The codec factory can be reused between multiple writers.
     *
     * Returned writer can be used emit multiple top level element,
     * but does not start / close parent JSON object, which must be done
     * by user providing {@code jsonWriter} instance in order for
     * JSON to be valid.
     *
     * @param codecFactory JSON codec factory
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory, final SchemaPath path, final URI initialNs, final JsonWriter jsonWriter) {
        return new JSONNormalizedNodeStreamWriter(codecFactory, path, jsonWriter, new JSONStreamWriterSharedRootContext(initialNs));
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        final JSONCodec<Object> codec = codecs.codecFor(schema);
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
        final JSONCodec<Object> codec = codecs.codecFor(schema);
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
        final SchemaNode schema = tracker.startContainerNode(name);

        // FIXME this code ignores presence for containers
        // but datastore does as well and it needs be fixed first (2399)
        context = new JSONStreamWriterNamedObjectContext(context, name, DEFAULT_EMIT_EMPTY_CONTAINERS);
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
        // FIXME this kind of serialization is incorrect since the value for AnyXml is now a DOMSource
        writer.value(String.valueOf(value));
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startYangModeledAnyXmlNode(name);
        context = new JSONStreamWriterNamedObjectContext(context, name, true);
    }

    @Override
    public void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(codecs.getSchemaContext(), writer);

        if(context instanceof JSONStreamWriterRootContext) {
            context.emitEnd(writer);
        }
    }

    private void writeValue(final Object value, final JSONCodec<Object> codec)
            throws IOException {
        codec.serializeToWriter(writer,value);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
    }
}
