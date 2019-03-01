/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Collections;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class SchemaAwareXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<SchemaNode>
        implements SchemaContextProvider {
    private final SchemaTracker tracker;
    private final SchemaAwareXMLStreamWriterUtils streamUtils;

    SchemaAwareXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer, final SchemaContext context,
            final SchemaTracker tracker) {
        super(writer);
        this.tracker = requireNonNull(tracker);
        this.streamUtils = new SchemaAwareXMLStreamWriterUtils(context);
    }

    @Override
    void writeValue(final ValueWriter xmlWriter, final Object value, final SchemaNode schemaNode)
            throws XMLStreamException {
        streamUtils.writeValue(xmlWriter, schemaNode, value, schemaNode.getQName().getModule());
    }

    @Override
    void startList(final NodeIdentifier name) {
        tracker.startList(name);
    }

    @Override
    void startListItem(final PathArgument name) throws IOException {
        tracker.startListItem(name);
        startElement(name.getNodeType());
    }

    @Override
    public void endNode() throws IOException {
        final Object schema = tracker.endNode();
        if (schema instanceof ListSchemaNode) {
            // For lists, we only emit end element on the inner frame
            final Object parent = tracker.getParent();
            if (parent == schema) {
                endElement();
            }
        } else if (schema instanceof ContainerSchemaNode) {
            // Emit container end element
            endElement();
        }
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        tracker.startLeafNode(name);
        startElement(name.getNodeType());
    }


    @Override
    public void startLeafSetEntryNode(final QName name) throws IOException {
        tracker.startLeafSetEntryNode(name);
        startElement(name);

        // TODO Auto-generated method stub

    }
    @Override
    public void leafSetEntryNode(final QName name, final Object value) throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode(name);
        writeElement(schema.getQName(), value, Collections.emptyMap(), schema);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);
        startElement(schema.getQName());
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        tracker.startAugmentationNode(identifier);
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startYangModeledAnyXmlNode(name);
        startElement(schema.getQName());
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) throws IOException {
        tracker.startAnyxmlNode(name);
        startElement(name.getNodeType());
    }

    @Override
    public SchemaContext getSchemaContext() {
        return streamUtils.getSchemaContext();
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        final Object current = tracker.getParent();
        checkState(current instanceof LeafSchemaNode || current instanceof AnyXmlSchemaNode
            || current instanceof LeafListSchemaNode, "Unexpected scala value %s with %s", value, current);
        final SchemaNode schema = (SchemaNode) current;
        writeValue(value, schema);
    }
}
