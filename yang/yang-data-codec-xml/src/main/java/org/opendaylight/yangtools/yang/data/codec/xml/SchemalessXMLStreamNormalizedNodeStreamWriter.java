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
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class SchemalessXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<Object> {
    private enum ContainerType {
        CONTAINER,
        LEAF_SET,
        LIST,
        LIST_ITEM,
        ANY_XML,
        CHOICE,
        AUGMENTATION
    }

    private final Deque<ContainerType> containerTypeStack = new ArrayDeque<>();

    SchemalessXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer) {
        super(writer);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value, final Map<QName, String> attributes)
            throws IOException {
        writeElement(name.getNodeType(), value, attributes, null);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        writeElement(name.getNodeType(), value, Collections.emptyMap(), null);
    }

    @Override
    public void leafSetEntryNode(final QName name, final Object value, final Map<QName, String> attributes)
            throws IOException {
        writeElement(name, value, attributes, null);
    }

    @Override
    public void leafSetEntryNode(final QName name, final Object value) throws IOException {
        writeElement(name, value, Collections.emptyMap(), null);
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
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        anyxmlNode(name.getNodeType(), value);
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    void writeValue(final XMLStreamWriter xmlWriter, final QName qname, final Object value, final Object context)
            throws XMLStreamException {
        final String str = value.toString();
        if (!str.isEmpty()) {
            xmlWriter.writeCharacters(str);
        }
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
    public void endNode() throws IOException {
        ContainerType type = containerTypeStack.pop();
        switch (type) {
            case CONTAINER:
            case LIST_ITEM:
            case ANY_XML:
                endElement();
                break;
            default:
                break;
        }
    }
}
