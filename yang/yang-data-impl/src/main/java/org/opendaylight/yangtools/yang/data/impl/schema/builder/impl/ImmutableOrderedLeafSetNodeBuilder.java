/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableOrderedLeafSetNodeBuilder<T> implements ListNodeBuilder<T, LeafSetEntryNode<T>> {

    private final MutableLeafSetMap<T> value;
    private YangInstanceIdentifier.NodeIdentifier nodeIdentifier;

    protected ImmutableOrderedLeafSetNodeBuilder() {
        value = new MutableLeafSetMap<>();
    }

    protected ImmutableOrderedLeafSetNodeBuilder(final ImmutableOrderedLeafSetNode<T> node) {
        nodeIdentifier = node.getIdentifier();
        value = node.children.toModifiableMap();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create() {
        return new ImmutableOrderedLeafSetNodeBuilder<>();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafSetNode<T> node) {
        if (!(node instanceof ImmutableOrderedLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableOrderedLeafSetNodeBuilder<T>((ImmutableOrderedLeafSetNode<T>) node);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(final LeafSetEntryNode<T> child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withoutChild(final PathArgument key) {
        this.value.remove(key);
        return this;
    }

    @Override
    public OrderedLeafSetNode<T> build() {
        return new ImmutableOrderedLeafSetNode<>(nodeIdentifier, value.toUnmodifiableMap());
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(
            final YangInstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withValue(final Collection<LeafSetEntryNode<T>> value) {
        for (final LeafSetEntryNode<T> leafSetEntry : value) {
            withChild(leafSetEntry);
        }
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value, final Map<QName, String> attributes) {
        final ImmutableLeafSetEntryNodeBuilder<T> b = ImmutableLeafSetEntryNodeBuilder.create();
        b.withNodeIdentifier(new YangInstanceIdentifier.NodeWithValue(nodeIdentifier.getNodeType(), value));
        b.withValue(value);
        b.withAttributes(attributes);
        return withChild(b.build());
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value) {
        return withChildValue(value, Collections.<QName,String>emptyMap());
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, LeafSetEntryNode<T>, LeafSetNode<T>> addChild(
            final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, LeafSetEntryNode<T>, LeafSetNode<T>> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableOrderedLeafSetNode<T> extends
            AbstractImmutableNormalizedNode<YangInstanceIdentifier.NodeIdentifier, Collection<LeafSetEntryNode<T>>> implements
            Immutable, OrderedLeafSetNode<T> {

        private final ImmutableLeafSetMap<T> children;

        ImmutableOrderedLeafSetNode(final YangInstanceIdentifier.NodeIdentifier nodeIdentifier,
                final ImmutableLeafSetMap<T> children) {
            super(nodeIdentifier);
            this.children = Preconditions.checkNotNull(children);
        }

        @Override
        public Optional<LeafSetEntryNode<T>> getChild(final YangInstanceIdentifier.NodeWithValue child) {
            return Optional.fromNullable(children.get(child));
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return children.equals(((ImmutableOrderedLeafSetNode<?>) other).children);
        }

        @Override
        public LeafSetEntryNode<T> getChild(final int position) {
            return Iterables.get(children.values(), position);
        }

        @Override
        public int getSize() {
            return children.size();
        }

        @Override
        public Collection<LeafSetEntryNode<T>> getValue() {
            return UnmodifiableCollection.create(children.values());
        }
    }
}
