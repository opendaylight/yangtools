/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream.ANYXML_ARRAY_ELEMENT_ID;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.common.QName;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This implementation will create JSON output as output stream.
 *
 * <p>
 * Values of leaf and leaf-list are NOT translated according to codecs.
 */
public abstract class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final class Exclusive extends JSONNormalizedNodeStreamWriter {
        Exclusive(final JSONCodecFactory codecFactory, final SchemaPath path, final JsonWriter writer,
                final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, path, writer, rootContext);
        }

        @Override
        public void close() throws IOException {
            flush();
            closeWriter();
        }
    }

    private static final class Nested extends JSONNormalizedNodeStreamWriter {
        Nested(final JSONCodecFactory codecFactory, final SchemaPath path, final JsonWriter writer,
                final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, path, writer, rootContext);
        }

        @Override
        public void close() throws IOException {
            flush();
            // The caller "owns" the writer, let them close it
        }
    }

    /**
     * RFC6020 deviation: we are not required to emit empty containers unless they
     * are marked as 'presence'.
     */
    private static final boolean DEFAULT_EMIT_EMPTY_CONTAINERS = true;

    @RegEx
    private static final String NUMBER_STRING = "-?\\d+(\\.\\d+)?";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_STRING);

    @RegEx
    private static final String NOT_DECIMAL_NUMBER_STRING = "-?\\d+";
    private static final Pattern NOT_DECIMAL_NUMBER_PATTERN = Pattern.compile(NOT_DECIMAL_NUMBER_STRING);

    private final SchemaTracker tracker;
    private final JSONCodecFactory codecs;
    private final JsonWriter writer;
    private JSONStreamWriterContext context;

    JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory, final SchemaPath path, final JsonWriter writer,
            final JSONStreamWriterRootContext rootContext) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecFactory);
        this.tracker = SchemaTracker.create(codecFactory.getSchemaContext(), path);
        this.context = requireNonNull(rootContext);
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * <p>
     * The codec factory can be reused between multiple writers.
     *
     * <p>
     * Returned writer is exclusive user of JsonWriter, which means it will start
     * top-level JSON element and ends it.
     *
     * <p>
     * This instance of writer can be used only to emit one top level element,
     * otherwise it will produce incorrect JSON. Closing this instance will close
     * the writer too.
     *
     * @param codecFactory JSON codec factory
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final SchemaPath path, final URI initialNs, final JsonWriter jsonWriter) {
        return new Exclusive(codecFactory, path, jsonWriter, new JSONStreamWriterExclusiveRootContext(initialNs));
    }

    /**
     * Create a new stream writer, which writes to the specified output stream.
     *
     * <p>
     * The codec factory can be reused between multiple writers.
     *
     * <p>
     * Returned writer can be used emit multiple top level element,
     * but does not start / close parent JSON object, which must be done
     * by user providing {@code jsonWriter} instance in order for
     * JSON to be valid. Closing this instance <strong>will not</strong>
     * close the wrapped writer; the caller must take care of that.
     *
     * @param codecFactory JSON codec factory
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final SchemaPath path, final URI initialNs, final JsonWriter jsonWriter) {
        return new Nested(codecFactory, path, jsonWriter, new JSONStreamWriterSharedRootContext(initialNs));
    }

    @Override
    public final void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        final JSONCodec<?> codec = codecs.codecFor(schema);
        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
        writeValue(value, codec);
    }

    @Override
    public final void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public final void leafSetEntryNode(final QName name, final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode(name);
        final JSONCodec<?> codec = codecs.codecFor(schema);
        context.emittingChild(codecs.getSchemaContext(), writer);
        writeValue(value, codec);
    }

    @Override
    public final void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    /*
     * Warning suppressed due to static final constant which triggers a warning
     * for the call to schema.isPresenceContainer().
     */
    @SuppressWarnings("unused")
    @Override
    public final void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);

        // FIXME this code ignores presence for containers
        // but datastore does as well and it needs be fixed first (2399)
        context = new JSONStreamWriterNamedObjectContext(context, name, DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public final void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public final void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startListItem(name);
        context = new JSONStreamWriterObjectContext(context, name, DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public final void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public final void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        tracker.startListItem(identifier);
        context = new JSONStreamWriterObjectContext(context, identifier, DEFAULT_EMIT_EMPTY_CONTAINERS);
    }

    @Override
    public final void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public final void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
        context = new JSONStreamWriterInvisibleContext(context);
    }

    @Override
    public final void startAugmentationNode(final AugmentationIdentifier identifier) {
        tracker.startAugmentationNode(identifier);
        context = new JSONStreamWriterInvisibleContext(context);
    }

    @Override
    public final void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        @SuppressWarnings("unused")
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        // FIXME: should have a codec based on this :)

        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());

        writeAnyXmlValue((DOMSource) value);
    }

    @Override
    public final void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint)
            throws IOException {
        tracker.startYangModeledAnyXmlNode(name);
        context = new JSONStreamWriterNamedObjectContext(context, name, true);
    }

    @Override
    public final void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(codecs.getSchemaContext(), writer);

        if (context instanceof JSONStreamWriterRootContext) {
            context.emitEnd(writer);
        }
    }

    @Override
    public final void flush() throws IOException {
        writer.flush();
    }

    final void closeWriter() throws IOException {
        writer.close();
    }

    @SuppressWarnings("unchecked")
    private void writeValue(final Object value, final JSONCodec<?> codec) throws IOException {
        ((JSONCodec<Object>) codec).writeValue(writer, value);
    }

    private void writeAnyXmlValue(final DOMSource anyXmlValue) throws IOException {
        writeXmlNode(anyXmlValue.getNode());
    }

    private void writeXmlNode(final Node node) throws IOException {
        final Element firstChildElement = getFirstChildElement(node);
        if (firstChildElement == null) {
            writeXmlValue(node);
        } else if (ANYXML_ARRAY_ELEMENT_ID.equals(firstChildElement.getNodeName())) {
            writer.beginArray();
            writeArray(firstChildElement);
            writer.endArray();
        } else {
            writer.beginObject();
            writeObject(firstChildElement);
            writer.endObject();
        }
    }

    private void writeArray(Node node) throws IOException {
        while (node != null) {
            if (ELEMENT_NODE == node.getNodeType()) {
                writeXmlNode(node);
            }
            node = node.getNextSibling();
        }
    }

    private void writeObject(Node node) throws IOException {
        while (node != null) {
            if (ELEMENT_NODE == node.getNodeType()) {
                writer.name(node.getNodeName());
                writeXmlNode(node);
            }
            node = node.getNextSibling();
        }
    }

    private void writeXmlValue(final Node node) throws IOException {
        Text firstChild = getFirstChildText(node);
        String childNodeText = firstChild != null ? firstChild.getWholeText() : "";
        childNodeText = childNodeText != null ? childNodeText.trim() : "";

        if (NUMBER_PATTERN.matcher(childNodeText).matches()) {
            writer.value(parseNumber(childNodeText));
            return;
        }
        switch (childNodeText) {
            case "null":
                writer.nullValue();
                break;
            case "false":
                writer.value(false);
                break;
            case "true":
                writer.value(true);
                break;
            default:
                writer.value(childNodeText);
        }
    }

    private static Element getFirstChildElement(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            final Node childNode = children.item(i);
            if (ELEMENT_NODE == childNode.getNodeType()) {
                return (Element) childNode;
            }
        }
        return null;
    }

    private static Text getFirstChildText(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            final Node childNode = children.item(i);
            if (TEXT_NODE == childNode.getNodeType()) {
                return (Text) childNode;
            }
        }
        return null;
    }

    // json numbers are 64 bit wide floating point numbers - in java terms it is either long or double
    private static Number parseNumber(final String numberText) {
        if (NOT_DECIMAL_NUMBER_PATTERN.matcher(numberText).matches()) {
            return Long.valueOf(numberText);
        }

        return Double.valueOf(numberText);
    }
}
