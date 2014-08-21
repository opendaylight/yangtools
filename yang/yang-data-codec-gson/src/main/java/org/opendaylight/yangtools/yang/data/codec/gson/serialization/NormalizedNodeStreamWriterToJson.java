/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Stack;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.SchemaContextUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 *
 * This implementation will create JSON output as output stream.
 *
 * Values of leaf and leaf-list are NOT translated according to codecs.
 *
 */
public class NormalizedNodeStreamWriterToJson implements NormalizedNodeStreamWriter {

    private enum NodeType {
        object,
        list,
        irrelevant
    }

    private class TypeInfo {
        private NodeType type;
        private boolean hasAtLeastOneChild = false;
        private URI uri;

        public TypeInfo(final NodeType type, final URI uri) {
            this.type = type;
            this.uri = uri;
        }

        public void setHasAtLeastOneChild(boolean hasChildren) {
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

    private int indentSize = 2;
    private int indentLevel = 0;

    private OutputStream outputStream;

    private Stack<TypeInfo> stack = new Stack<>();
    private boolean prettyPrint = false;
    private SchemaContextUtils schemaContextUtils;
    private URI currentNamespace = null;

    public NormalizedNodeStreamWriterToJson(final boolean prettyPrint, final SchemaContext schemaContext,
            final OutputStream outputStream) {
        super();
        this.prettyPrint = prettyPrint;
        schemaContextUtils = SchemaContextUtils.getInstance();
        schemaContextUtils.setSchemaContext(schemaContext);
        this.outputStream = outputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void leafNode(NodeIdentifier name, Object value) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            writeJsonIdentifier(name);
            currentNamespace = stack.peek().getNamespace();
            writeValue(value.toString());
            separateNextSiblingsWithComma();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }
    }

    @Override
    public void startLeafSet(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            stack.push(new TypeInfo(NodeType.list, name.getNodeType().getNamespace()));
            writeJsonIdentifier(name);
            writeStartList();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }
    }

    @Override
    public void leafSetEntryNode(Object value) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            writeValue(value.toString());
            separateNextSiblingsWithComma();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(value, e);
        }
    }

    @Override
    public void startContainerNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            stack.push(new TypeInfo(NodeType.object, name.getNodeType().getNamespace()));
            writeJsonIdentifier(name);
            writeStartObject();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }
    }

    @Override
    public void startUnkeyedList(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            stack.push(new TypeInfo(NodeType.list, name.getNodeType().getNamespace()));
            writeJsonIdentifier(name);
            writeStartList();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }

    }

    @Override
    public void startUnkeyedListItem(NodeIdentifier name, int childSizeHint) throws IllegalStateException {
        try {
            stack.push(new TypeInfo(NodeType.object, name.getNodeType().getNamespace()));
            separateElementFromPreviousElement();
            writeStartObject();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }

    }

    @Override
    public void startMapNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            stack.push(new TypeInfo(NodeType.list, name.getNodeType().getNamespace()));
            writeJsonIdentifier(name);
            writeStartList();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }

    }

    @Override
    public void startMapEntryNode(NodeIdentifierWithPredicates identifier, int childSizeHint)
            throws IllegalArgumentException {
        try {
            stack.push(new TypeInfo(NodeType.object, identifier.getNodeType().getNamespace()));
            separateElementFromPreviousElement();
            writeStartObject();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(identifier, e);
        }
    }

    @Override
    public void startOrderedMapNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        try {
            stack.push(new TypeInfo(NodeType.list, name.getNodeType().getNamespace()));
            separateElementFromPreviousElement();
            writeJsonIdentifier(name);
            writeStartList();
            indentRigth();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }
    }

    @Override
    public void startChoiceNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException {
        handleInvisibleNode(name.getNodeType().getNamespace());
    }

    @Override
    public void startAugmentationNode(AugmentationIdentifier identifier) throws IllegalArgumentException {
        handleInvisibleNode(currentNamespace);
    }

    @Override
    public void anyxmlNode(NodeIdentifier name, Object value) throws IllegalArgumentException {
        try {
            separateElementFromPreviousElement();
            writeJsonIdentifier(name);
            currentNamespace = stack.peek().getNamespace();
            writeValue(value.toString());
            separateNextSiblingsWithComma();
        } catch (IOException e) {
            throw new OutputStreamWrittingException(name, e);
        }
    }

    @Override
    public void endNode() throws IllegalStateException {
        switch (stack.peek().getType()) {
        case list:
            indentLeft();
            try {
                newLine();
                outputStream.write("]".getBytes());
            } catch (IOException e) {
                throw new OutputStreamWrittingException("Wasn't possible to write to output stream.", e);
            }
            break;
        case object:
            indentLeft();
            try {
                newLine();
                outputStream.write("}".getBytes());
            } catch (IOException e) {
                throw new OutputStreamWrittingException("Wasn't possible to write to output stream.", e);
            }
            break;
        default:
            break;
        }
        stack.pop();
        currentNamespace = stack.empty() ? null : stack.peek().getNamespace();
        separateNextSiblingsWithComma();
    }

    private void separateElementFromPreviousElement() throws IOException {
        if (!stack.empty() && stack.peek().hasAtLeastOneChild()) {
            outputStream.write(",".getBytes());
        }
        newLine();
    }

    private void newLine() throws IOException {
        if (prettyPrint) {
            outputStream.write("\n".getBytes());
        }
        outputStream.write(ind().getBytes());
    }

    private void separateNextSiblingsWithComma() {
        if (!stack.empty()) {
            stack.peek().setHasAtLeastOneChild(true);
        }
    }

    /**
     * Invisible nodes have to be also pushed to stack because of pairing of start*() and endNode() methods. Information
     * about child existing (due to printing comma) has to be transfered to invisible node.
     */
    private void handleInvisibleNode(final URI uri) {
        TypeInfo typeInfo = new TypeInfo(NodeType.irrelevant, uri);
        typeInfo.setHasAtLeastOneChild(stack.peek().hasAtLeastOneChild());
        stack.push(typeInfo);
    }

    private void writeStartObject() throws IOException {
        outputStream.write("{".getBytes());
    }

    private void writeStartList() throws IOException {
        outputStream.write("[".getBytes());
    }

    private void writeModulName(final URI namespace) throws IOException {
        if (this.currentNamespace == null || namespace != this.currentNamespace) {
            Module module = schemaContextUtils.findModuleByNamespace(namespace);
            outputStream.write(module.getName().getBytes());
            outputStream.write(":".getBytes());
            currentNamespace = namespace;
        }
    }

    private void writeValue(String value) throws IOException {
        outputStream.write("\"".getBytes());
        outputStream.write(value.getBytes());
        outputStream.write("\"".getBytes());
    }

    private void writeJsonIdentifier(NodeIdentifier name) throws IOException {
        outputStream.write("\"".getBytes());
        writeModulName(name.getNodeType().getNamespace());
        outputStream.write(name.getNodeType().getLocalName().getBytes());
        outputStream.write("\"".getBytes());
        outputStream.write(":".getBytes());
    }

    private String ind() {
        final StringBuilder builder = new StringBuilder();
        if (prettyPrint) {
            for (int i = 0; i < indentLevel * indentSize; i++) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private void indentRigth() {
        indentLevel++;
    }

    private void indentLeft() {
        indentLevel--;
    }

}
