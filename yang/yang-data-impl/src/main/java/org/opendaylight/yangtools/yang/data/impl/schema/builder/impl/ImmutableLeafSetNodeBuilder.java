/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class ImmutableLeafSetNodeBuilder<T> implements ListNodeBuilder<T, LeafSetEntryNode<T>> {

    private Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> value;
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private boolean dirty;

    protected ImmutableLeafSetNodeBuilder() {
        value = new LinkedHashMap<>();
        dirty = false;
    }

    protected ImmutableLeafSetNodeBuilder(final ImmutableLeafSetNode<T> node) {
        value = node.getChildren();
        nodeIdentifier = node.getIdentifier();
        dirty = true;
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafSetNode<T> node) {
        if (!(node instanceof ImmutableLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableLeafSetNodeBuilder<T>((ImmutableLeafSetNode<T>) node);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(final LeafSetEntryNode<T> child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public LeafSetNode<T> build() {
        dirty = true;
        return new ImmutableLeafSetNode<>(nodeIdentifier, value);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(
            final InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withValue(final List<LeafSetEntryNode<T>> value) {
        checkDirty();
        for (final LeafSetEntryNode<T> leafSetEntry : value) {
            withChild(leafSetEntry);
        }

        return this;
    }


    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value, final Map<QName, String> attributes) {
        final ImmutableLeafSetEntryNodeBuilder<T> b = ImmutableLeafSetEntryNodeBuilder.create();
        b.withNodeIdentifier(new InstanceIdentifier.NodeWithValue(nodeIdentifier.getNodeType(), value));
        b.withValue(value);
        b.withAttributes(attributes);
        return withChild(b.build());
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value) {
        return withChildValue(value, Collections.<QName,String>emptyMap());
    }

    protected final static class ImmutableLeafSetNode<T> extends
            AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<LeafSetEntryNode<T>>> implements
            Immutable, LeafSetNode<T> {

        private final Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> children;

        ImmutableLeafSetNode(final InstanceIdentifier.NodeIdentifier nodeIdentifier,
                final Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> children) {
            super(nodeIdentifier, Iterables.unmodifiableIterable(children.values()));
            this.children = children;
        }

        @Override
        public Optional<LeafSetEntryNode<T>> getChild(final InstanceIdentifier.NodeWithValue child) {
            return Optional.fromNullable(children.get(child));
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        private Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> getChildren() {
            return Collections.unmodifiableMap(children);
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return children.equals(((ImmutableLeafSetNode<?>) other).children);
        }
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

}
