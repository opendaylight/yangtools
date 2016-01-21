/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.SchemaAwareNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class BindingToNormalizedStreamWriter implements BindingStreamEventWriter, Delegator<NormalizedNodeStreamWriter> {
    private static final class Schemaless extends BindingToNormalizedStreamWriter {
        private final NormalizedNodeStreamWriter delegate;

        Schemaless(final NodeCodecContext<?> schema, final NormalizedNodeStreamWriter delegate) {
            super(schema);
            this.delegate = Preconditions.checkNotNull(delegate, "Delegate must not be null");
        }

        @Override
        public NormalizedNodeStreamWriter getDelegate() {
            return delegate;
        }

        @Override
        protected void emitSchema(final Object schema) {
            // No-op
        }
    }

    private static final class SchemaAware extends BindingToNormalizedStreamWriter {
        private final SchemaAwareNormalizedNodeStreamWriter delegate;

        SchemaAware(final NodeCodecContext<?> schema, final SchemaAwareNormalizedNodeStreamWriter delegate) {
            super(schema);
            this.delegate = Preconditions.checkNotNull(delegate, "Delegate must not be null");
        }

        @Override
        public SchemaAwareNormalizedNodeStreamWriter getDelegate() {
            return delegate;
        }

        @Override
        protected void emitSchema(final Object schema) {
            delegate.nextDataSchemaNode((DataSchemaNode) schema);
        }
    }

    private final Deque<NodeCodecContext<?>> schema = new ArrayDeque<>();
    private final NodeCodecContext<?> rootNodeSchema;

    BindingToNormalizedStreamWriter(final NodeCodecContext<?> schema) {
        this.rootNodeSchema = Preconditions.checkNotNull(schema);
    }

    static BindingToNormalizedStreamWriter create(final NodeCodecContext<?> schema, final NormalizedNodeStreamWriter delegate) {
        if (delegate instanceof SchemaAwareNormalizedNodeStreamWriter) {
            return new SchemaAware(schema, (SchemaAwareNormalizedNodeStreamWriter) delegate);
        } else {
            return new Schemaless(schema, delegate);
        }
    }

    protected abstract void emitSchema(Object schema);

    protected final NodeCodecContext<?> current() {
        return schema.peek();
    }

    private NodeIdentifier duplicateSchemaEnter() {
        final NodeCodecContext<?> next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            next = current();
        }
        this.schema.push(next);
        return (NodeIdentifier) current().getDomPathArgument();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends YangInstanceIdentifier.PathArgument> T enter(final Class<?> name, final Class<T> identifier) {
        final NodeCodecContext<?> next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            Preconditions.checkArgument((current() instanceof DataContainerCodecContext), "Could not start node %s",
                    name);
            next = ((DataContainerCodecContext<?,?>) current()).streamChild((Class) name);
        }
        this.schema.push(next);
        T arg = (T) next.getDomPathArgument();
        return arg;
    }

    private <T extends YangInstanceIdentifier.PathArgument> T enter(final String localName, final Class<T> identifier) {
        NodeCodecContext<?> current = current();
        NodeCodecContext<?> next = ((DataObjectCodecContext<?,?>) current).getLeafChild(localName);
        this.schema.push(next);
        @SuppressWarnings("unchecked")
        T arg = (T) next.getDomPathArgument();
        return arg;
    }

    @Override
    public final void endNode() throws IOException {
        NodeCodecContext<?> left = schema.pop();
        // NormalizedNode writer does not have entry into case, but into choice
        // so for leaving case, we do not emit endNode.
        if (!(left instanceof CaseNodeCodecContext)) {
            getDelegate().endNode();
        }
    }

    private Map.Entry<NodeIdentifier, Object> serializeLeaf(final String localName, final Object value) {
        Preconditions.checkArgument(current() instanceof DataObjectCodecContext);

        DataObjectCodecContext<?,?> currentCasted = (DataObjectCodecContext<?,?>) current();
        LeafNodeCodecContext<?> leafContext = currentCasted.getLeafChild(localName);

        NodeIdentifier domArg = (NodeIdentifier) leafContext.getDomPathArgument();
        Object domValue = leafContext.getValueCodec().serialize(value);
        emitSchema(leafContext.getSchema());
        return new AbstractMap.SimpleEntry<>(domArg, domValue);
    }

    @Override
    public final void leafNode(final String localName, final Object value) throws IOException {
        Entry<NodeIdentifier, Object> dom = serializeLeaf(localName, value);
        getDelegate().leafNode(dom.getKey(), dom.getValue());
    }

    @Override
    public final void anyxmlNode(final String name, final Object value) throws IOException {
        Entry<NodeIdentifier, Object> dom = serializeLeaf(name, value);
        getDelegate().anyxmlNode(dom.getKey(), dom.getValue());
    }

    @Override
    public final void leafSetEntryNode(final Object value) throws IOException {
        LeafNodeCodecContext<?> ctx = (LeafNodeCodecContext<?>) current();
        getDelegate().leafSetEntryNode(((DataSchemaNode) ctx.getSchema()).getQName(),
            ctx.getValueCodec().serialize(value));
    }

    @Override
    public final void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType)
            throws IOException {
        getDelegate().startAugmentationNode(enter(augmentationType, AugmentationIdentifier.class));
    }

    @Override
    public final void startCase(final Class<? extends DataObject> caze, final int childSizeHint) {
        enter(caze, NodeIdentifier.class);
    }

    @Override
    public final void startChoiceNode(final Class<? extends DataContainer> type, final int childSizeHint)
            throws IOException {
        getDelegate().startChoiceNode(enter(type, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final void startContainerNode(final Class<? extends DataObject> object, final int childSizeHint)
            throws IOException {
        getDelegate().startContainerNode(enter(object, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final void startLeafSet(final String localName, final int childSizeHint) throws IOException {
        final NodeIdentifier id = enter(localName, NodeIdentifier.class);
        emitSchema(current().getSchema());
        getDelegate().startLeafSet(id, childSizeHint);
    }

    @Override
    public final void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException {
        getDelegate().startOrderedLeafSet(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final void startMapEntryNode(final Identifier<?> key, final int childSizeHint) throws IOException {
        duplicateSchemaEnter();
        NodeIdentifierWithPredicates identifier = ((KeyedListNodeCodecContext<?>) current()).serialize(key);
        getDelegate().startMapEntryNode(identifier, childSizeHint);
    }

    @Override
    public final <T extends DataObject & Identifiable<?>> void startMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException {
        getDelegate().startMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final <T extends DataObject & Identifiable<?>> void startOrderedMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException {
        getDelegate().startOrderedMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final void startUnkeyedList(final Class<? extends DataObject> obj, final int childSizeHint) throws IOException {
        getDelegate().startUnkeyedList(enter(obj, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public final void startUnkeyedListItem(final int childSizeHint) throws IOException {
        getDelegate().startUnkeyedListItem(duplicateSchemaEnter(), childSizeHint);
    }

    @Override
    public final void flush() throws IOException {
        getDelegate().flush();
    }

    @Override
    public final void close() throws IOException {
        getDelegate().close();
    }
}
