/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class BindingToNormalizedStreamWriter implements AnydataBindingStreamWriter,
        Delegator<NormalizedNodeStreamWriter> {
    private final Deque<CodecContext> schema = new ArrayDeque<>();
    private final @NonNull NormalizedNodeStreamWriter delegate;
    private final CodecContext rootContext;

    BindingToNormalizedStreamWriter(final DataContainerCodecContext<?, ?, ?> rootContext,
            final NormalizedNodeStreamWriter delegate) {
        this.rootContext = requireNonNull(rootContext);
        this.delegate = requireNonNull(delegate);
    }

    private void emitSchema(final Object schemaNode) {
        delegate.nextDataSchemaNode((DataSchemaNode) schemaNode);
    }

    CodecContext current() {
        return schema.peek();
    }

    private NodeIdentifier duplicateSchemaEnter() {
        final var current = current();
        final CodecContext next;
        if (current == null) {
            // Entry of first node
            next = rootContext;
        } else {
            next = current;
        }
        schema.push(next);
        return next.getDomPathArgument();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends PathArgument> T enter(final Class<?> name, final Class<T> identifier) {
        final var current = current();
        final CodecContext next;
        if (current == null) {
            // Entry of first node
            next = rootContext;
        } else if (current instanceof DataContainerCodecContext<?, ?, ?> currentContainer) {
            next = currentContainer.getStreamChild((Class) name);
        } else {
            throw new IllegalArgumentException("Could not start node " + name + " in non-container " + current);
        }
        schema.push(next);
        return identifier.cast(next.getDomPathArgument());
    }

    private <T extends PathArgument> T enter(final String localName, final Class<T> identifier) {
        final var current = current();
        final var next = ((AbstractDataObjectCodecContext<?, ?>) current).getLeafChild(localName);
        schema.push(next);
        return identifier.cast(next.getDomPathArgument());
    }

    @Override
    public NormalizedNodeStreamWriter getDelegate() {
        return delegate;
    }

    @Override
    public void endNode() throws IOException {
        CodecContext left = schema.pop();
        // Due to writer does not start a new node on startCase() and on startAugmentationNode()
        // node ending should not be triggered when associated endNode() is invoked.
        if (!(left instanceof CaseCodecContext) && !(left instanceof AugmentationCodecContext)) {
            delegate.endNode();
        }
    }

    private Map.Entry<NodeIdentifier, Object> serializeLeaf(final String localName, final Object value) {
        final var current = current();
        if (!(current instanceof AbstractDataObjectCodecContext<?, ?> currentCasted)) {
            throw new IllegalArgumentException("Unexpected current context " + current);
        }

        ValueNodeCodecContext leafContext = currentCasted.getLeafChild(localName);
        NodeIdentifier domArg = leafContext.getDomPathArgument();
        Object domValue = leafContext.getValueCodec().serialize(value);
        emitSchema(leafContext.getSchema());
        return new AbstractMap.SimpleEntry<>(domArg, domValue);
    }

    @Override
    public void leafNode(final String localName, final Object value) throws IOException {
        final Entry<NodeIdentifier, Object> dom = serializeLeaf(localName, value);
        delegate.startLeafNode(dom.getKey());
        delegate.scalarValue(dom.getValue());
        delegate.endNode();
    }

    @Override
    public void anydataNode(final String name, final OpaqueObject<?> value) throws IOException {
        final Entry<NodeIdentifier, Object> dom = serializeLeaf(name, value);
        if (delegate.startAnydataNode(dom.getKey(), value.getValue().getObjectModel())) {
            delegate.scalarValue(dom.getValue());
            delegate.endNode();
        }
    }

    @Override
    public void anyxmlNode(final String name, final Object value) throws IOException {
        final Entry<NodeIdentifier, Object> dom = serializeLeaf(name, value);
        // FIXME: this is not quite right -- we should be handling other object models, too
        if (delegate.startAnyxmlNode(dom.getKey(), DOMSource.class)) {
            delegate.domSourceValue((DOMSource) dom.getValue());
            delegate.endNode();
        }
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        final LeafSetNodeCodecContext ctx = (LeafSetNodeCodecContext) current();
        final Object domValue = ctx.getValueCodec().serialize(value);
        delegate.startLeafSetEntryNode(new NodeWithValue<>(ctx.getSchema().getQName(), domValue));
        delegate.scalarValue(domValue);
        delegate.endNode();
    }

    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType) throws IOException {
        enter(augmentationType, NodeIdentifier.class);
    }

    @Override
    public void startCase(final Class<? extends DataObject> caze, final int childSizeHint) {
        enter(caze, NodeIdentifier.class);
    }

    @Override
    public void startChoiceNode(final Class<? extends DataContainer> type, final int childSizeHint)
            throws IOException {
        delegate.startChoiceNode(enter(type, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startContainerNode(final Class<? extends DataObject> object, final int childSizeHint)
            throws IOException {
        delegate.startContainerNode(enter(object, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException {
        final NodeIdentifier id = enter(localName, NodeIdentifier.class);
        emitSchema(current().getSchema());
        delegate.startLeafSet(id, childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException {
        delegate.startOrderedLeafSet(enter(localName, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startMapEntryNode(final Key<?> key, final int childSizeHint) throws IOException {
        duplicateSchemaEnter();
        NodeIdentifierWithPredicates identifier = ((MapCodecContext<?, ?>) current()).serialize(key);
        delegate.startMapEntryNode(identifier, childSizeHint);
    }

    @Override
    public <T extends DataObject & KeyAware<?>> void startMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IOException {
        delegate.startMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public <T extends DataObject & KeyAware<?>> void startOrderedMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException {
        delegate.startOrderedMapNode(enter(mapEntryType, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startUnkeyedList(final Class<? extends DataObject> obj, final int childSizeHint) throws IOException {
        delegate.startUnkeyedList(enter(obj, NodeIdentifier.class), childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IOException {
        delegate.startUnkeyedListItem(duplicateSchemaEnter(), childSizeHint);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
