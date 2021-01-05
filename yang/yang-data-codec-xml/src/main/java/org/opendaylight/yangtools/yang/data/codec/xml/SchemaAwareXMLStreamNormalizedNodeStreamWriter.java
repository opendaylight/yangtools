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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class SchemaAwareXMLStreamNormalizedNodeStreamWriter
        extends XMLStreamNormalizedNodeStreamWriter<TypedDataSchemaNode> implements EffectiveModelContextProvider {
    private final SchemaTracker tracker;
    private final SchemaAwareXMLStreamWriterUtils streamUtils;
    private final SchemaInferenceStack node;


    SchemaAwareXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer, final EffectiveModelContext context,
            final SchemaTracker tracker) {
        super(writer);
        this.tracker = requireNonNull(tracker);
        this.streamUtils = new SchemaAwareXMLStreamWriterUtils(context);
        this.node = new SchemaInferenceStack(streamUtils.getEffectiveModelContext());
    }

    @Override
    String encodeValue(final ValueWriter xmlWriter, final Object value, final TypedDataSchemaNode schemaNode,
            final SchemaInferenceStack stackNode)
            throws XMLStreamException {
        return streamUtils.encodeValue(xmlWriter, schemaNode, schemaNode.getType(), value,
            schemaNode.getQName().getModule(), stackNode);
    }

    @Override
    String encodeAnnotationValue(final ValueWriter xmlWriter, final QName qname, final Object value)
            throws XMLStreamException {
        final Optional<AnnotationSchemaNode> optAnnotation =
            AnnotationSchemaNode.find(streamUtils.getEffectiveModelContext(), qname);
        if (optAnnotation.isPresent()) {
            final AnnotationSchemaNode schema = optAnnotation.get();
            final SchemaInferenceStack emptyStack = new SchemaInferenceStack(streamUtils.getEffectiveModelContext());
            return streamUtils.encodeValue(xmlWriter, schema, schema.getType(), value, qname.getModule(), emptyStack);
        }

        checkArgument(!qname.getRevision().isPresent(), "Failed to find bound annotation %s", qname);
        checkArgument(value instanceof String, "Invalid non-string value %s for unbound annotation %s", value, qname);
        return (String) value;
    }

    @Override
    void startList(final NodeIdentifier name) {
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
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
            } else {
                node.exit();
            }
        } else if (schema instanceof ContainerLike || schema instanceof LeafSchemaNode
                || schema instanceof AnydataSchemaNode || schema instanceof AnyxmlSchemaNode) {
            endElement();
            node.exit();
        }

        if (schema instanceof ChoiceSchemaNode) {
            node.exit();
        }

        if (!node.isEmpty() && node.currentStatement() instanceof CaseEffectiveStatement) {
            node.exit();
        }
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        tracker.startLeafNode(name);
        node.enterSchemaTree(nodeType);
        startElement(nodeType);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        tracker.startLeafSetEntryNode(name);
        startElement(name.getNodeType());
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        tracker.startLeafSet(name);
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        final SchemaNode schema = tracker.startContainerNode(name);
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
        startElement(schema.getQName());
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        tracker.startAugmentationNode(identifier);
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (DOMSource.class.isAssignableFrom(objectModel)) {
            tracker.startAnyxmlNode(name);
            final QName nodeType = name.getNodeType();
            findChildInCases(nodeType);
            node.enterSchemaTree(nodeType);
            startElement(nodeType);
            return true;
        }
        return false;
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return streamUtils.getEffectiveModelContext();
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        final Object current = tracker.getParent();
        if (current instanceof TypedDataSchemaNode) {
            writeValue(value, (TypedDataSchemaNode) current, node);
        } else if (current instanceof AnydataSchemaNode) {
            anydataValue(value);
        } else {
            throw new IllegalStateException("Unexpected scalar value " + value + " with " + current);
        }
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final Object current = tracker.getParent();
        checkState(current instanceof AnyxmlSchemaNode, "Unexpected scala value %s with %s", value, current);
        anyxmlValue(value);
    }

    @Override
    void startAnydata(final NodeIdentifier name) {
        tracker.startAnydataNode(name);
        final QName nodeType = name.getNodeType();
        findChildInCases(nodeType);
        node.enterSchemaTree(nodeType);
    }

    private void findChildInCases(final QName qname) {
        if (!node.isEmpty()) {
            final List<CaseEffectiveStatement> collect = node.currentStatement()
                    .streamEffectiveSubstatements(CaseEffectiveStatement.class).collect(Collectors.toList());
            for (final CaseEffectiveStatement caze : collect) {
                final Optional<?> potential = caze.findSchemaTreeNode(qname);
                if (potential.isPresent()) {
                    node.enterSchemaTree(caze.argument());
                }
            }
        }
    }
}
