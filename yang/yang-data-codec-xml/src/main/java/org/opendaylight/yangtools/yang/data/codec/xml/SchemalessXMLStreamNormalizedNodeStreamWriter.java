/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class SchemalessXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<Object> {
    private enum NodeType {
        CONTAINER,
        LEAF_SET,
        LIST,
        LIST_ITEM,
        YANG_MODELED_ANY_XML,
        CHOICE,
        AUGMENTATION,
        SCALAR,
        ANY_XML,
    }

    private final Deque<NodeType> nodeTypeStack = new ArrayDeque<>();

    SchemalessXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer) {
        super(writer);
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        nodeTypeStack.push(NodeType.SCALAR);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        nodeTypeStack.push(NodeType.SCALAR);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        nodeTypeStack.push(NodeType.LEAF_SET);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        nodeTypeStack.push(NodeType.LEAF_SET);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        nodeTypeStack.push(NodeType.CONTAINER);
        startElement(name.getNodeType());
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        nodeTypeStack.push(NodeType.CHOICE);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IOException {
        nodeTypeStack.push(NodeType.AUGMENTATION);
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) throws IOException {
        nodeTypeStack.push(NodeType.ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        nodeTypeStack.push(NodeType.YANG_MODELED_ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    void writeValue(final ValueWriter xmlWriter, final Object value, final Object context) throws XMLStreamException {
        xmlWriter.writeToStringCharacters(value);
    }

    @Override
    void startList(final NodeIdentifier name) {
        nodeTypeStack.push(NodeType.LIST);
    }

    @Override
    void startListItem(final PathArgument name) throws IOException {
        nodeTypeStack.push(NodeType.LIST_ITEM);
        startElement(name.getNodeType());
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        final NodeType type = nodeTypeStack.peek();
        checkState(type == NodeType.SCALAR, "Unexpected scalar %s in %s", value, type);
        writeValue(value, null);
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final NodeType type = nodeTypeStack.peek();
        checkState(type == NodeType.ANY_XML, "Unexpected DOMSource %s in %s", value, type);
        anyxmlValue(value);
    }

    @Override
    public void endNode() throws IOException {
        NodeType type = nodeTypeStack.pop();
        switch (type) {
            case CONTAINER:
            case LIST_ITEM:
            case SCALAR:
            case ANY_XML:
                endElement();
                break;
            default:
                break;
        }
    }
}
