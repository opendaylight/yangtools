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

class BindingToNormalizedStreamWriter implements BindingStreamEventWriter, Delegator<NormalizedNodeStreamWriter> {

    private final NormalizedNodeStreamWriter delegate;
    private final Deque<NodeCodecContext> schema = new ArrayDeque<>();
    private final NodeCodecContext rootNodeSchema;

    public BindingToNormalizedStreamWriter(final NodeCodecContext schema, final NormalizedNodeStreamWriter delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "Delegate must not be null");
        this.rootNodeSchema = Preconditions.checkNotNull(schema);

    }

    private NodeCodecContext current() {
        return schema.peek();
    }

    private NodeIdentifier duplicateSchemaEnter() {
        final NodeCodecContext next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            next = current();
        }
        this.schema.push(next);
        return (NodeIdentifier) current().getDomPathArgument();
    }

    private <T extends YangInstanceIdentifier.PathArgument> T enter(final Class<?> name, final Class<T> identifier) {
        final NodeCodecContext next;
        if (current() == null) {
            // Entry of first node
            next = rootNodeSchema;
        } else {
            Preconditions.checkArgument((current() instanceof DataContainerCodecContext), "Could not start node %s",
                    name);
            next = ((DataContainerCodecContext<?>) current()).getStreamChild(name);
        }
        this.schema.push(next);
        @SuppressWarnings("unchecked")
        T arg = (T) next.getDomPathArgument();
        return arg;
    }

    private <T extends YangInstanceIdentifier.PathArgument> T enter(final String localName, final Class<T> identifier) {
        NodeCodecContext current = current();
        NodeCodecContext next = ((DataObjectCodecContext<?>) current).getLeafChild(localName);
        this.schema.push(next);
        @SuppressWarnings("unchecked")
        T arg = (T) next.getDomPathArgument();
        return arg;
    }

    @Override
    public void endNode() throws IOException {
        NodeCodecContext left = schema.pop();
        // NormalizedNode writer does not have entry into case, but into choice
        // so for leaving case, we do not emit endNode.
        if (!(left instanceof CaseNodeCodecContext)) {
            getDelegate().endNode();
        }
    }

    @Override
    public NormalizedNodeStreamWriter getDelegate() {
        return delegate;
    }

    private Map.Entry<NodeIdentifier, Object> serializeLeaf(final String localName, final Object value) {
        Preconditions.checkArgument(current() instanceof DataObjectCodecContext<?>);

        DataObjectCodecContext<?> currentCasted = (DataObjectCodecContext<?>) current();
        LeafNodeCodecContext leafContext = currentCasted.getLeafChild(localName);

        NodeIdentifier domArg = (NodeIdentifier) leafContext.getDomPathArgument();
        Object domValue = leafContext.getValueCodec().serialize(value);
        return new AbstractMap.SimpleEntry<>(domArg, domValue);
    }

    @Override
    public void leafNode(final String localName, final Object value) throws IOException, IllegalArgumentException {
        Entry<NodeIdentifier, Object> dom = serializeLeaf(localName, value);
        getDelegate().leafNode(dom.getKey(), dom.getValue());
    };

    @Override
    public void anyxmlNode(final String name, final Object value) throws IOException, IllegalArgumentException {
        Entry<NodeIdentifier, Object> dom = serializeLeaf(name, value);
        getDelegate().anyxmlNode(dom.getKey(), dom.getValue());
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException, IllegalArgumentException {
        LeafNodeCodecContext ctx = (LeafNodeCodecContext) current();
        getDelegate().leafSetEntryNode(ctx.getValueCodec().serialize(value));
    }

    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType)
            throws IOException, IllegalArgumentException {
        getDelegate().startAugmentationNode(enter(augmentationType, AugmentationIdentifier.class));
    }

    @Override
    public void startCase(final Class<? extends DataObject> caze, final int childSizeHint)
            throws IllegalArgumentException {
        enter(caze, NodeIdentifier.class);
    };

    @Override
    public void startChoiceNode(final Class<? extends DataContainer> type, final int childSizeHint)
            throws IOException, IllegalArgumentException {
        getDelegate().startChoiceNode(enter(type, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startContainerNode(final Class<? extends DataObject> object, final int childSizeHint)
            throws IOException, IllegalArgumentException {
        getDelegate().startContainerNode(enter(object, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException, IllegalArgumentException {
        getDelegate().startLeafSet(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException, IllegalArgumentException {
        getDelegate().startOrderedLeafSet(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startMapEntryNode(final Identifier<?> key, final int childSizeHint) throws IOException, IllegalArgumentException {
        duplicateSchemaEnter();
        NodeIdentifierWithPredicates identifier = ((ListNodeCodecContext) current()).serialize(key);
        getDelegate().startMapEntryNode(identifier, childSizeHint);
    }

    @Override
    public <T extends DataObject & Identifiable<?>> void startMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException, IllegalArgumentException {
        getDelegate().startMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    };

    @Override
    public <T extends DataObject & Identifiable<?>> void startOrderedMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException, IllegalArgumentException {
        getDelegate().startOrderedMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    };

    @Override
    public void startUnkeyedList(final Class<? extends DataObject> obj, final int childSizeHint)
            throws IOException, IllegalArgumentException {
        getDelegate().startUnkeyedList(enter(obj, NodeIdentifier.class), childSizeHint);
    };

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IllegalStateException, IOException {
        getDelegate().startUnkeyedListItem(duplicateSchemaEnter(), childSizeHint);
    }

    @Override
    public void flush() throws IOException {
        getDelegate().flush();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }
}
