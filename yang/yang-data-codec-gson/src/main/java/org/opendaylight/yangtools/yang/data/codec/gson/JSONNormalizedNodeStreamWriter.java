/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;
import javax.xml.transform.dom.DOMSource;
import org.checkerframework.checker.regex.qual.Regex;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
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
        Exclusive(final JSONCodecFactory codecFactory, final SchemaTracker tracker, final JsonWriter writer,
                final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, tracker, writer, rootContext);
        }

        @Override
        public void close() throws IOException {
            flush();
            closeWriter();
        }
    }

    private static final class Nested extends JSONNormalizedNodeStreamWriter {
        Nested(final JSONCodecFactory codecFactory, final SchemaTracker tracker, final JsonWriter writer,
                final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, tracker, writer, rootContext);
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

    @Regex
    private static final String NUMBER_STRING = "-?\\d+(\\.\\d+)?";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_STRING);

    @Regex
    private static final String NOT_DECIMAL_NUMBER_STRING = "-?\\d+";
    private static final Pattern NOT_DECIMAL_NUMBER_PATTERN = Pattern.compile(NOT_DECIMAL_NUMBER_STRING);

    private final SchemaTracker tracker;
    private final JSONCodecFactory codecs;
    private final JsonWriter writer;
    private JSONStreamWriterContext context;

    JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory, final SchemaTracker tracker,
            final JsonWriter writer, final JSONStreamWriterRootContext rootContext) {
        this.writer = requireNonNull(writer);
        this.codecs = requireNonNull(codecFactory);
        this.tracker = requireNonNull(tracker);
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
        return new Exclusive(codecFactory, SchemaTracker.create(codecFactory.getSchemaContext(), path), jsonWriter,
            new JSONStreamWriterExclusiveRootContext(initialNs));
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
     * @param rootNode Root node
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final DataNodeContainer rootNode, final URI initialNs, final JsonWriter jsonWriter) {
        return new Exclusive(codecFactory, SchemaTracker.create(rootNode), jsonWriter,
            new JSONStreamWriterExclusiveRootContext(initialNs));
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
        return new Nested(codecFactory, SchemaTracker.create(codecFactory.getSchemaContext(), path), jsonWriter,
            new JSONStreamWriterSharedRootContext(initialNs));
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
     * @param rootNode Root node
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final DataNodeContainer rootNode, final URI initialNs, final JsonWriter jsonWriter) {
        return new Nested(codecFactory, SchemaTracker.create(rootNode), jsonWriter,
            new JSONStreamWriterSharedRootContext(initialNs));
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        tracker.startLeafNode(name);
        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
    }

    @Override
    public final void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        tracker.startLeafSetEntryNode(name);
        context.emittingChild(codecs.getSchemaContext(), writer);
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
    @Override
    public final void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);
        final boolean isPresence = schema instanceof ContainerSchemaNode
            ? ((ContainerSchemaNode) schema).isPresenceContainer() : DEFAULT_EMIT_EMPTY_CONTAINERS;
        context = new JSONStreamWriterNamedObjectContext(context, name, isPresence);
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
    public final void startAnyxmlNode(final NodeIdentifier name) throws IOException {
        tracker.startAnyxmlNode(name);
        context.emittingChild(codecs.getSchemaContext(), writer);
        context.writeChildJsonIdentifier(codecs.getSchemaContext(), writer, name.getNodeType());
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

    @Override
    public void scalarValue(final Object value) throws IOException {
        final Object current = tracker.getParent();
        checkState(current instanceof TypedDataSchemaNode, "Cannot emit scalar %s for %s", value, current);
        writeValue(value, codecs.codecFor((TypedDataSchemaNode) current));
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final Object current = tracker.getParent();
        checkState(current instanceof AnyXmlSchemaNode, "Cannot emit DOMSource %s for %s", value, current);
        // FIXME: should have a codec based on this :)
        writeAnyXmlValue(value);
    }

    @SuppressWarnings("unchecked")
    private void writeValue(final Object value, final JSONCodec<?> codec) throws IOException {
        ((JSONCodec<Object>) codec).writeValue(writer, value);
    }

    private void writeAnyXmlValue(final DOMSource anyXmlValue) throws IOException {
        writeXmlNode(anyXmlValue.getNode());
    }

    private void writeXmlNode(final Node node) throws IOException {
        if (isArrayElement(node)) {
            writeArrayContent(node);
            return;
        }
        final Element firstChildElement = getFirstChildElement(node);
        if (firstChildElement == null) {
            writeXmlValue(node);
        } else {
            writeObjectContent(firstChildElement);
        }
    }

    private void writeArrayContent(final Node node) throws IOException {
        writer.beginArray();
        handleArray(node);
        writer.endArray();
    }

    private void writeObjectContent(final Element firstChildElement) throws IOException {
        writer.beginObject();
        writeObject(firstChildElement);
        writer.endObject();
    }

    private static boolean isArrayElement(final Node node) {
        if (ELEMENT_NODE == node.getNodeType()) {
            final String nodeName = node.getNodeName();
            for (Node nextNode = node.getNextSibling(); nextNode != null; nextNode = nextNode.getNextSibling()) {
                if (ELEMENT_NODE == nextNode.getNodeType() && nodeName.equals(nextNode.getNodeName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleArray(final Node node) throws IOException {
        final Element parentNode = (Element)node.getParentNode();
        final NodeList elementsList = parentNode.getElementsByTagName(node.getNodeName());
        for (int i = 0, length = elementsList.getLength(); i < length; i++) {
            final Node arrayElement = elementsList.item(i);
            final Element parent = (Element)arrayElement.getParentNode();
            if (parentNode.isSameNode(parent)) {
                final Element firstChildElement = getFirstChildElement(arrayElement);
                if (firstChildElement != null) {
                    writeObjectContent(firstChildElement);
                } else {
                    // It may be scalar
                    writeXmlValue(arrayElement);
                }
            }
        }
    }

    private void writeObject(Node node) throws IOException {
        String previousNodeName = "";
        while (node != null) {
            if (ELEMENT_NODE == node.getNodeType()) {
                if (!node.getNodeName().equals(previousNodeName)) {
                    previousNodeName = node.getNodeName();
                    writer.name(node.getNodeName());
                    writeXmlNode(node);
                }
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
