/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

class SchemalessXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<Object> {
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

    private SchemalessXMLStreamNormalizedNodeStreamWriter(XMLStreamWriter writer) {
        super(writer);
    }

    static NormalizedNodeStreamWriter newInstance(XMLStreamWriter writer) {
        return new SchemalessXMLStreamNormalizedNodeStreamWriter(writer);
    }

    @Override
    public void leafNode(NodeIdentifier name, Object value, Map<QName, String> attributes) throws IOException {
        writeElement(name.getNodeType(), value, attributes, null);
    }

    @Override
    public void leafSetEntryNode(QName name, Object value, Map<QName, String> attributes) throws IOException {
        writeElement(name, value, attributes, null);
    }

    @Override
    public void leafNode(NodeIdentifier name, Object value) throws IOException {
        writeElement(name.getNodeType(), value, Collections.emptyMap(), null);
    }

    @Override
    public void leafSetEntryNode(QName name, Object value) throws IOException {
        writeElement(name, value, Collections.emptyMap(), null);
    }

    @Override
    public void startLeafSet(NodeIdentifier name, int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.LEAF_SET);
    }

    @Override
    public void startOrderedLeafSet(NodeIdentifier name, int childSizeHint)
            throws IOException, IllegalArgumentException {
        containerTypeStack.push(ContainerType.LEAF_SET);
    }

    @Override
    public void startContainerNode(NodeIdentifier name, int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.CONTAINER);
        startElement(name.getNodeType());
    }

    @Override
    public void startChoiceNode(NodeIdentifier name, int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.CHOICE);
    }

    @Override
    public void startAugmentationNode(AugmentationIdentifier identifier) throws IOException {
        containerTypeStack.push(ContainerType.AUGMENTATION);
    }

    @Override
    public void anyxmlNode(NodeIdentifier name, Object value) throws IOException {
        anyxmlNode(name.getNodeType(), value);
    }

    @Override
    public void startYangModeledAnyXmlNode(NodeIdentifier name, int childSizeHint) throws IOException {
        containerTypeStack.push(ContainerType.ANY_XML);
        startElement(name.getNodeType());
    }

    @Override
    protected void writeValue(XMLStreamWriter xmlWriter, QName qname, @Nonnull Object value, Object context)
            throws XMLStreamException {
        xmlWriter.writeCharacters(value.toString());
    }

    @Override
    protected void startList(NodeIdentifier name) {
        containerTypeStack.push(ContainerType.LIST);
    }

    @Override
    protected void startListItem(PathArgument name) throws IOException {
        containerTypeStack.push(ContainerType.LIST_ITEM);
        startElement(name.getNodeType());
    }

    @Override
    protected void endNode(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
        ContainerType type = containerTypeStack.pop();
        switch(type) {
        case CONTAINER:
        case LIST_ITEM:
        case ANY_XML:
            xmlWriter.writeEndElement();
            break;
        default:
            break;
        }
    }
}
