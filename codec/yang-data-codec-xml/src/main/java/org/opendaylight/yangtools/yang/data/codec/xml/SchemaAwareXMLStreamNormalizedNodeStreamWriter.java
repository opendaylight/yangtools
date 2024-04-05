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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.NormalizedNodeStreamWriterStack;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

final class SchemaAwareXMLStreamNormalizedNodeStreamWriter
        extends XMLStreamNormalizedNodeStreamWriter<TypedDataSchemaNode> {
    private final NormalizedNodeStreamWriterStack tracker;
    private final SchemaAwareXMLStreamWriterUtils streamUtils;

    private SchemaAwareXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer,
            final EffectiveModelContext modelContext, final NormalizedNodeStreamWriterStack tracker,
            final @Nullable PreferredPrefixes pref) {
        super(writer, pref);
        this.tracker = requireNonNull(tracker);
        streamUtils = new SchemaAwareXMLStreamWriterUtils(modelContext, pref);
    }

    SchemaAwareXMLStreamNormalizedNodeStreamWriter(final XMLStreamWriter writer,
            final EffectiveModelContext modelContext, final NormalizedNodeStreamWriterStack tracker,
            final boolean modelPrefixes) {
        this(writer, modelContext, tracker, modelPrefixes ? new PreferredPrefixes.Shared(modelContext) : null);
    }

    @Override
    String encodeValue(final ValueWriter xmlWriter, final Object value, final TypedDataSchemaNode schemaNode)
            throws XMLStreamException {
        return streamUtils.encodeValue(xmlWriter, resolveType(schemaNode.getType()), value,
            schemaNode.getQName().getModule());
    }

    @Override
    String encodeAnnotationValue(final ValueWriter xmlWriter, final QName qname, final Object value)
            throws XMLStreamException {
        final var optAnnotation = AnnotationSchemaNode.find(streamUtils.modelContext(), new AnnotationName(qname));
        if (optAnnotation.isPresent()) {
            return streamUtils.encodeValue(xmlWriter, resolveType(optAnnotation.orElseThrow().getType()), value,
                qname.getModule());
        }

        if (qname.getRevision().isPresent()) {
            throw new IllegalArgumentException("Failed to find bound annotation " + qname);
        }
        if (value instanceof String str) {
            return str;
        }
        throw new IllegalArgumentException("Invalid non-string value " + value + " for unbound annotation " + qname);
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
        final var schema = tracker.endNode();
        if (schema instanceof ListEffectiveStatement || schema instanceof LeafListEffectiveStatement) {
            // For lists, we only emit end element on the inner frame
            if (tracker.currentStatement() == schema) {
                endElement();
            }
        } else if (schema instanceof ContainerEffectiveStatement || schema instanceof LeafEffectiveStatement
                || schema instanceof AnydataEffectiveStatement || schema instanceof AnyxmlEffectiveStatement
                || schema instanceof InputEffectiveStatement || schema instanceof OutputEffectiveStatement
                || schema instanceof NotificationEffectiveStatement) {
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
        tracker.startContainerNode(name);
        startElement(name.getNodeType());
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        tracker.startChoiceNode(name);
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (DOMSource.class.isAssignableFrom(objectModel)) {
            tracker.startAnyxmlNode(name);
            startElement(name.getNodeType());
            return true;
        }
        return false;
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        final var current = tracker.currentStatement();
        if (current instanceof TypedDataSchemaNode typedSchema) {
            writeValue(value, typedSchema);
        } else if (current instanceof AnydataEffectiveStatement) {
            anydataValue(value);
        } else {
            throw new IllegalStateException("Unexpected scalar value " + value + " with " + current);
        }
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final var current = tracker.currentStatement();
        checkState(current instanceof AnyxmlEffectiveStatement, "Unexpected value %s with %s", value, current);
        anyxmlValue(value);
    }

    @Override
    void startAnydata(final NodeIdentifier name) {
        tracker.startAnydataNode(name);
    }

    private @NonNull TypeDefinition<?> resolveType(final @NonNull TypeDefinition<?> type) throws XMLStreamException {
        if (type instanceof LeafrefTypeDefinition leafref) {
            try {
                return tracker.resolveLeafref(leafref);
            } catch (IllegalArgumentException e) {
                throw new XMLStreamException("Cannot resolve type " + type, e);
            }
        }
        return type;
    }
}
