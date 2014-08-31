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
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.RestCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.SchemaContextUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * This implementation will create JSON output as output stream.
 *
 * Values of leaf and leaf-list are NOT translated according to codecs.
 *
 * FIXME: rewrite this in terms of {@link JsonWriter}.
 */
public class JSONNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {

    private static enum NodeType {
        OBJECT,
        LIST,
        OTHER,
    }

    private static class TypeInfo {
        private boolean hasAtLeastOneChild = false;
        private final NodeType type;
        private final URI uri;

        public TypeInfo(final NodeType type, final URI uri) {
            this.type = type;
            this.uri = uri;
        }

        public void setHasAtLeastOneChild(final boolean hasChildren) {
            this.hasAtLeastOneChild = hasChildren;
        }

        public NodeType getType() {
            return type;
        }

        public URI getNamespace() {
            return uri;
        }

        public boolean hasAtLeastOneChild() {
            return hasAtLeastOneChild;
        }
    }

    private final Deque<TypeInfo> stack = new ArrayDeque<>();
    private final SchemaContext schemaContext;
    private final SchemaContextUtils utils;
    private final RestCodecFactory codecs;
    private final SchemaTracker tracker;
    private final Writer writer;
    private final String indent;

    private URI currentNamespace = null;
    private int currentDepth = 0;

    private JSONNormalizedNodeStreamWriter(final SchemaContext schemaContext,
            final Writer writer, final int indentSize) {
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
        this.writer = Preconditions.checkNotNull(writer);

        Preconditions.checkArgument(indentSize >= 0, "Indent size must be non-negative");
        if (indentSize != 0) {
            indent = Strings.repeat(" ", indentSize);
        } else {
            indent = null;
        }

        this.utils = SchemaContextUtils.create(schemaContext);
        this.codecs = RestCodecFactory.create(utils);
        this.tracker = SchemaTracker.create(schemaContext);
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

        separateElementFromPreviousElement();
        writeJsonIdentifier(name);
        currentNamespace = stack.peek().getNamespace();
        writeValue(String.valueOf(codec.serialize(value)));
        separateNextSiblingsWithComma();
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startLeafSet(name);

        separateElementFromPreviousElement();
        stack.push(new TypeInfo(NodeType.LIST, name.getNodeType().getNamespace()));
        writeJsonIdentifier(name);
        writeStartList();
        indentRight();
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode();
        final Codec<Object, Object> codec = codecs.codecFor(schema.getType());

        separateElementFromPreviousElement();
        writeValue(String.valueOf(codec.serialize(value)));
        separateNextSiblingsWithComma();
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        separateElementFromPreviousElement();
        stack.push(new TypeInfo(NodeType.OBJECT, name.getNodeType().getNamespace()));
        writeJsonIdentifier(name);
        writeStartObject();
        indentRight();
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);

        separateElementFromPreviousElement();
        stack.push(new TypeInfo(NodeType.LIST, name.getNodeType().getNamespace()));
        writeJsonIdentifier(name);
        writeStartList();
        indentRight();
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startListItem(name);

        stack.push(new TypeInfo(NodeType.OBJECT, name.getNodeType().getNamespace()));
        separateElementFromPreviousElement();
        writeStartObject();
        indentRight();
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startList(name);

        separateElementFromPreviousElement();
        stack.push(new TypeInfo(NodeType.LIST, name.getNodeType().getNamespace()));
        writeJsonIdentifier(name);
        writeStartList();
        indentRight();
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        tracker.startListItem(identifier);

        stack.push(new TypeInfo(NodeType.OBJECT, identifier.getNodeType().getNamespace()));
        separateElementFromPreviousElement();
        writeStartObject();
        indentRight();
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        tracker.startListItem(name);

        stack.push(new TypeInfo(NodeType.LIST, name.getNodeType().getNamespace()));
        separateElementFromPreviousElement();
        writeJsonIdentifier(name);
        writeStartList();
        indentRight();
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        tracker.startChoiceNode(name);
        handleInvisibleNode(name.getNodeType().getNamespace());
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IllegalArgumentException {
        tracker.startAugmentationNode(identifier);
        handleInvisibleNode(currentNamespace);
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        // FIXME: should have a codec based on this :)

        separateElementFromPreviousElement();
        writeJsonIdentifier(name);
        currentNamespace = stack.peek().getNamespace();
        writeValue(value.toString());
        separateNextSiblingsWithComma();
    }

    @Override
    public void endNode() throws IOException {
        switch (stack.peek().getType()) {
        case LIST:
            indentLeft();
            newLine();
            writer.append(']');
            break;
        case OBJECT:
            indentLeft();
            newLine();
            writer.append('}');
            break;
        default:
            break;
        }

        tracker.endNode();
        stack.pop();
        currentNamespace = stack.isEmpty() ? null : stack.peek().getNamespace();
        separateNextSiblingsWithComma();
    }

    private void separateElementFromPreviousElement() throws IOException {
        if (!stack.isEmpty() && stack.peek().hasAtLeastOneChild()) {
            writer.append(',');
        }
        newLine();
    }

    private void newLine() throws IOException {
        if (indent != null) {
            writer.append('\n');

            for (int i = 0; i < currentDepth; i++) {
                writer.append(indent);
            }
        }
    }

    private void separateNextSiblingsWithComma() {
        if (!stack.isEmpty()) {
            stack.peek().setHasAtLeastOneChild(true);
        }
    }

    /**
     * Invisible nodes have to be also pushed to stack because of pairing of start*() and endNode() methods. Information
     * about child existing (due to printing comma) has to be transfered to invisible node.
     */
    private void handleInvisibleNode(final URI uri) {
        TypeInfo typeInfo = new TypeInfo(NodeType.OTHER, uri);
        typeInfo.setHasAtLeastOneChild(stack.peek().hasAtLeastOneChild());
        stack.push(typeInfo);
    }

    private void writeStartObject() throws IOException {
        writer.append('{');
    }

    private void writeStartList() throws IOException {
        writer.append('[');
    }

    private void writeModulName(final URI namespace) throws IOException {
        if (this.currentNamespace == null || namespace != this.currentNamespace) {
            Module module = schemaContext.findModuleByNamespaceAndRevision(namespace, null);
            writer.append(module.getName());
            writer.append(':');
            currentNamespace = namespace;
        }
    }

    private void writeValue(final String value) throws IOException {
        writer.append('"');
        writer.append(value);
        writer.append('"');
    }

    private void writeJsonIdentifier(final NodeIdentifier name) throws IOException {
        writer.append('"');
        writeModulName(name.getNodeType().getNamespace());
        writer.append(name.getNodeType().getLocalName());
        writer.append("\":");
    }

    private void indentRight() {
        currentDepth++;
    }

    private void indentLeft() {
        currentDepth--;
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
