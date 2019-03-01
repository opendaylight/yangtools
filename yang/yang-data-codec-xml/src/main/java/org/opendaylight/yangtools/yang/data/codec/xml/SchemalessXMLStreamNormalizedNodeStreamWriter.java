/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class SchemalessXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<Object> {
    private enum ContainerType {
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

    private final Deque<ContainerType> containerTypeStack = new ArrayDeque<>();

    SchemalessXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer) {
        super(writer);
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        containerTypeStack.push(ContainerType.SCALAR);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        containerTypeStack.push(ContainerType.SCALAR);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.LEAF_SET);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.LEAF_SET);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.CONTAINER);
        startElement(name.getNodeType());
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.CHOICE);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IOException {
        containerTypeStack.push(ContainerType.AUGMENTATION);
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) throws IOException {
        containerTypeStack.push(ContainerType.ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.YANG_MODELED_ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    void writeValue(final ValueWriter xmlWriter, final Object value, final Object context) throws XMLStreamException {
        xmlWriter.writeToStringCharacters(value);
    }

    @Override
    void startList(final NodeIdentifier name) {
        containerTypeStack.push(ContainerType.LIST);
    }

    @Override
    void startListItem(final PathArgument name) throws IOException {
        containerTypeStack.push(ContainerType.LIST_ITEM);
        startElement(name.getNodeType());
    }


    @Override
    public void nodeValue(Object value) throws IOException {
        final ContainerType type = containerTypeStack.peek();
        switch (type) {
            case ANY_XML:
                anyxmlValue(value);
                break;
            case SCALAR:
                writeValue(value, null);
                break;
            default:
                throw new IllegalStateException("Unexpected scalar in " + type);
        }
    }

    @Override
    public void endNode() throws IOException {
        ContainerType type = containerTypeStack.pop();
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
