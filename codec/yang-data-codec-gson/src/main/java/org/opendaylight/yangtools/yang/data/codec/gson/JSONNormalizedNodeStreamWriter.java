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

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import javax.xml.transform.dom.DOMSource;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MountPointExtension;
import org.opendaylight.yangtools.yang.data.util.NormalizedNodeStreamWriterStack;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
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
public abstract class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter, MountPointExtension {
    private static final class Exclusive extends JSONNormalizedNodeStreamWriter {
        Exclusive(final JSONCodecFactory codecFactory, final NormalizedNodeStreamWriterStack tracker,
                final JsonWriter writer, final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, tracker, writer, rootContext);
        }

        @Override
        public void close() throws IOException {
            flush();
            closeWriter();
        }
    }

    private static final class Nested extends JSONNormalizedNodeStreamWriter {
        Nested(final JSONCodecFactory codecFactory, final NormalizedNodeStreamWriterStack tracker,
                final JsonWriter writer, final JSONStreamWriterRootContext rootContext) {
            super(codecFactory, tracker, writer, rootContext);
        }

        @Override
        public void close() throws IOException {
            flush();
            // The caller "owns" the writer, let them close it
        }
    }

    /**
     * RFC6020 deviation: we are not required to emit empty containers unless they are marked as 'presence'.
     */
    private static final boolean DEFAULT_EMIT_EMPTY_CONTAINERS = true;

    @Regex
    private static final String NUMBER_STRING = "-?\\d+(\\.\\d+)?";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_STRING);

    @Regex
    private static final String NOT_DECIMAL_NUMBER_STRING = "-?\\d+";
    private static final Pattern NOT_DECIMAL_NUMBER_PATTERN = Pattern.compile(NOT_DECIMAL_NUMBER_STRING);

    private final NormalizedNodeStreamWriterStack tracker;
    private final JSONCodecFactory codecs;
    private final JsonWriter writer;
    private final DefaultJSONValueWriter valueWriter;

    private JSONStreamWriterContext context;

    private JSONNormalizedNodeStreamWriter(final JSONCodecFactory codecFactory,
            final NormalizedNodeStreamWriterStack tracker, final JsonWriter writer,
            final JSONStreamWriterRootContext rootContext) {
        this.writer = requireNonNull(writer);
        codecs = requireNonNull(codecFactory);
        this.tracker = requireNonNull(tracker);
        context = requireNonNull(rootContext);
        valueWriter = new DefaultJSONValueWriter(writer);
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
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final JsonWriter jsonWriter) {
        return createExclusiveWriter(codecFactory, jsonWriter, null);
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
     * @param jsonWriter JsonWriter
     * @param initialNs Initial namespace, can be null
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final JsonWriter jsonWriter, final @Nullable XMLNamespace initialNs) {
        return new Exclusive(codecFactory, NormalizedNodeStreamWriterStack.of(codecFactory.modelContext()), jsonWriter,
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
     * @param rootNode Root node inference
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final EffectiveStatementInference rootNode, final XMLNamespace initialNs, final JsonWriter jsonWriter) {
        return new Exclusive(codecFactory, NormalizedNodeStreamWriterStack.of(rootNode), jsonWriter,
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
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createExclusiveWriter(final JSONCodecFactory codecFactory,
            final Absolute path, final XMLNamespace initialNs, final JsonWriter jsonWriter) {
        return new Exclusive(codecFactory, NormalizedNodeStreamWriterStack.of(codecFactory.modelContext(), path),
            jsonWriter, new JSONStreamWriterExclusiveRootContext(initialNs));
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
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final JsonWriter jsonWriter) {
        return createNestedWriter(codecFactory, jsonWriter, null);
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
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final JsonWriter jsonWriter, final @Nullable XMLNamespace initialNs) {
        return new Nested(codecFactory, NormalizedNodeStreamWriterStack.of(codecFactory.modelContext()), jsonWriter,
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
     * @param path Schema Path
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final Absolute path, final XMLNamespace initialNs, final JsonWriter jsonWriter) {
        return new Nested(codecFactory, NormalizedNodeStreamWriterStack.of(codecFactory.modelContext(), path),
            jsonWriter, new JSONStreamWriterSharedRootContext(initialNs));
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
     * @param rootNode Root node inference
     * @param initialNs Initial namespace
     * @param jsonWriter JsonWriter
     * @return A stream writer instance
     */
    public static NormalizedNodeStreamWriter createNestedWriter(final JSONCodecFactory codecFactory,
            final EffectiveStatementInference rootNode, final XMLNamespace initialNs, final JsonWriter jsonWriter) {
        return new Nested(codecFactory, NormalizedNodeStreamWriterStack.of(rootNode), jsonWriter,
            new JSONStreamWriterSharedRootContext(initialNs));
    }

    @Override
    public Collection<? extends Extension> supportedExtensions() {
        return List.of(this);
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        tracker.startLeafNode(name);
        context.emittingChild(codecs.modelContext(), writer);
        context.writeChildJsonIdentifier(codecs.modelContext(), writer, name.getNodeType());
    }

    @Override
    public final void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        tracker.startLeafSetEntryNode(name);
        context.emittingChild(codecs.modelContext(), writer);
    }

    @Override
    public final void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);
        context = new JSONStreamWriterListContext(context, name);
    }

    @Override
    public final void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final boolean emitIfEmpty = tracker.startContainerNode(name) instanceof ContainerEffectiveStatement container
            ? container.findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isPresent()
                : DEFAULT_EMIT_EMPTY_CONTAINERS;
        context = new JSONStreamWriterNamedObjectContext(context, name, emitIfEmpty);
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
    public final boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (NormalizedAnydata.class.isAssignableFrom(objectModel)) {
            tracker.startAnydataNode(name);
            context.emittingChild(codecs.modelContext(), writer);
            context.writeChildJsonIdentifier(codecs.modelContext(), writer, name.getNodeType());
            return true;
        }

        return false;
    }

    @Override
    public final NormalizedNodeStreamWriter startMountPoint(final MountPointLabel label,
            final MountPointContext mountCtx) throws IOException {
        final EffectiveModelContext ctx = mountCtx.modelContext();
        return new Nested(codecs.rebaseTo(ctx), NormalizedNodeStreamWriterStack.of(ctx), writer,
            new JSONStreamWriterSharedRootContext(context.getNamespace()));
    }

    @Override
    public final boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (DOMSource.class.isAssignableFrom(objectModel)) {
            tracker.startAnyxmlNode(name);
            context.emittingChild(codecs.modelContext(), writer);
            context.writeChildJsonIdentifier(codecs.modelContext(), writer, name.getNodeType());
            return true;
        }
        return false;
    }

    @Override
    public final void endNode() throws IOException {
        tracker.endNode();
        context = context.endNode(codecs.modelContext(), writer);
    }

    @Override
    public final void flush() throws IOException {
        writer.flush();
    }

    final void closeWriter() throws IOException {
        if (!(context instanceof JSONStreamWriterRootContext)) {
            throw new IOException("Unexpected root context " + context);
        }

        context.endNode(codecs.modelContext(), writer);
        writer.close();
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        final var current = tracker.currentStatement();
        if (current instanceof TypedDataSchemaNode typed) {
            writeValue(value, codecs.codecFor(typed, tracker));
        } else if (current instanceof AnydataEffectiveStatement) {
            writeAnydataValue(value);
        } else {
            throw new IllegalStateException(String.format("Cannot emit scalar %s for %s", value, current));
        }
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final var current = tracker.currentStatement();
        checkState(current instanceof AnyxmlEffectiveStatement, "Cannot emit DOMSource %s for %s", value, current);
        // FIXME: should have a codec based on this :)
        writeAnyXmlValue(value);
    }

    @SuppressWarnings("unchecked")
    private void writeValue(final Object value, final JSONCodec<?> codec) throws IOException {
        ((JSONCodec<Object>) codec).writeValue(valueWriter, value);
    }

    private void writeAnydataValue(final Object value) throws IOException {
        if (value instanceof NormalizedAnydata normalized) {
            writeNormalizedAnydata(normalized);
        } else {
            throw new IllegalStateException("Unexpected anydata value " + value);
        }
    }

    private void writeNormalizedAnydata(final NormalizedAnydata anydata) throws IOException {
        // Adjust state to point to parent node and ensure it can handle data tree nodes
        final SchemaInferenceStack.Inference inference;
        try {
            final SchemaInferenceStack stack = SchemaInferenceStack.ofInference(anydata.getInference());
            stack.exitToDataTree();
            inference = stack.toInference();
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            throw new IOException("Cannot emit " + anydata, e);
        }

        anydata.writeTo(JSONNormalizedNodeStreamWriter.createNestedWriter(
            codecs.rebaseTo(inference.modelContext()), inference, context.getNamespace(), writer));
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
        if (Node.ELEMENT_NODE == node.getNodeType()) {
            final String nodeName = node.getNodeName();
            for (Node nextNode = node.getNextSibling(); nextNode != null; nextNode = nextNode.getNextSibling()) {
                if (Node.ELEMENT_NODE == nextNode.getNodeType() && nodeName.equals(nextNode.getNodeName())) {
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
        final var previousNodeNames = new HashSet<String>();
        while (node != null) {
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                final var nodeName = node.getNodeName();
                if (previousNodeNames.add(nodeName)) {
                    writer.name(nodeName);
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
            case "null" -> writer.nullValue();
            case "false" -> writer.value(false);
            case "true" -> writer.value(true);
            default -> writer.value(childNodeText);
        }
    }

    private static Element getFirstChildElement(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            final Node childNode = children.item(i);
            if (Node.ELEMENT_NODE == childNode.getNodeType()) {
                return (Element) childNode;
            }
        }
        return null;
    }

    private static Text getFirstChildText(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            final Node childNode = children.item(i);
            if (Node.TEXT_NODE == childNode.getNodeType()) {
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
