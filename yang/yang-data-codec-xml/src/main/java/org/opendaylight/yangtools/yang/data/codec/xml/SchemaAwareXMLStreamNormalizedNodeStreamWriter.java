/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;

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
    String encodeValue(final ValueWriter xmlWriter, final Object value, final SchemaNode schemaNode)
            throws XMLStreamException {
        return streamUtils.encodeValue(xmlWriter, schemaNode, value, schemaNode.getQName().getModule());
    }

    @Override
    String encodeAnnotationValue(final ValueWriter xmlWriter, final QName qname, final Object value)
            throws XMLStreamException {
        final Optional<AnnotationSchemaNode> optAnnotation = AnnotationSchemaNode.find(streamUtils.getSchemaContext(),
            qname);
        if (optAnnotation.isPresent()) {
            return streamUtils.encodeValue(xmlWriter, optAnnotation.get(), value, qname.getModule());
        }

        checkArgument(!qname.getRevision().isPresent(), "Failed to find bound annotation %s", qname);
        checkArgument(value instanceof String, "Invalid non-string value %s for unbound annotation %s", value, qname);
        return (String) value;
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
        if (schema instanceof ListSchemaNode || schema instanceof LeafListSchemaNode) {
            // For lists, we only emit end element on the inner frame
            final Object parent = tracker.getParent();
            if (parent == schema) {
                endElement();
            }
        } else if (schema instanceof ContainerSchemaNode || schema instanceof LeafSchemaNode
                || schema instanceof AnyDataSchemaNode) {
            endElement();
        }
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        tracker.startLeafNode(name);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        tracker.startLeafSetEntryNode(name);
        startElement(name.getNodeType());
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
        if (current instanceof TypedDataSchemaNode) {
            writeValue(value, (SchemaNode) current);
        } else if (current instanceof AnyDataSchemaNode) {
            anydataValue(value);
        } else {
            throw new IllegalStateException("Unexpected scalar value " + value + " with " + current);
        }
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final Object current = tracker.getParent();
        checkState(current instanceof AnyXmlSchemaNode, "Unexpected scala value %s with %s", value, current);
        anyxmlValue(value);
    }

    @Override
    void startAnydata(final NodeIdentifier name) {
        tracker.startAnydataNode(name);
    }
}
