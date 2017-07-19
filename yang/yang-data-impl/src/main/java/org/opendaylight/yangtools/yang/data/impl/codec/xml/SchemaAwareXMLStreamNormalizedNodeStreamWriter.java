/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.SchemaTracker;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Deprecated
final class SchemaAwareXMLStreamNormalizedNodeStreamWriter extends XMLStreamNormalizedNodeStreamWriter<SchemaNode> {
    private final SchemaTracker tracker;
    private final XmlStreamUtils streamUtils;

    private SchemaAwareXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer,
            final SchemaContext context, final SchemaPath path) {
        super(writer);
        this.tracker = SchemaTracker.create(context, path);
        this.streamUtils = XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER, context);
    }

    static NormalizedNodeStreamWriter newInstance(final XMLStreamWriter writer, final SchemaContext context,
            final SchemaPath path) {
        return new SchemaAwareXMLStreamNormalizedNodeStreamWriter(writer, context, path);
    }

    @Override
    protected void writeValue(final XMLStreamWriter xmlWriter, final QName qname, @Nonnull final Object value,
            final SchemaNode schemaNode) throws IOException, XMLStreamException {
        streamUtils.writeValue(xmlWriter, schemaNode, value, qname.getModule());
    }

    @Override
    protected void startList(final NodeIdentifier name) {
        tracker.startList(name);
    }

    @Override
    protected void startListItem(final PathArgument name) throws IOException {
        tracker.startListItem(name);
        startElement(name.getNodeType());
    }

    @Override
    protected void endNode(final XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
        final Object schema = tracker.endNode();
        if (schema instanceof ListSchemaNode) {
            // For lists, we only emit end element on the inner frame
            final Object parent = tracker.getParent();
            if (parent == schema) {
                xmlWriter.writeEndElement();
            }
        } else if (schema instanceof ContainerSchemaNode) {
            // Emit container end element
            xmlWriter.writeEndElement();
        }
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        writeElement(schema.getQName(), value, Collections.emptyMap(), schema);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value, final Map<QName, String> attributes)
            throws IOException {
        final LeafSchemaNode schema = tracker.leafNode(name);
        writeElement(schema.getQName(), value, attributes, schema);
    }

    @Override
    public void leafSetEntryNode(final QName name, final Object value, final Map<QName, String> attributes)
            throws IOException {
        final LeafListSchemaNode schema = tracker.leafSetEntryNode(name);
        writeElement(schema.getQName(), value, attributes, schema);
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
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        final AnyXmlSchemaNode schema = tracker.anyxmlNode(name);
        anyxmlNode(schema.getQName(), value);
    }
}
