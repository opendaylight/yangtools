/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class ImmutableLeafSetNodeBuilder<T> implements ListNodeBuilder<T, LeafSetEntryNode<T>> {

    protected Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> value = Maps.newLinkedHashMap();
    protected InstanceIdentifier.NodeIdentifier nodeIdentifier;

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(final LeafSetEntryNode<T> child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public LeafSetNode<T> build() {
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
        for (LeafSetEntryNode<T> leafSetEntry : value) {
            withChild(leafSetEntry);
        }

        return this;
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value) {
        return withChild(new ImmutableLeafSetEntryNodeBuilder.ImmutableLeafSetEntryNode<>(
                new InstanceIdentifier.NodeWithValue(nodeIdentifier.getNodeType(), value), value));
    }

    private final static class ImmutableLeafSetNode<T> extends
            AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<LeafSetEntryNode<T>>> implements
            Immutable, LeafSetNode<T> {

        private final Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> mappedChildren;

        ImmutableLeafSetNode(final InstanceIdentifier.NodeIdentifier nodeIdentifier,
                final Map<InstanceIdentifier.NodeWithValue, LeafSetEntryNode<T>> children) {
            super(nodeIdentifier, ImmutableList.copyOf(children.values()));
            this.mappedChildren = children;
        }

        @Override
        public Optional<LeafSetEntryNode<T>> getChild(final InstanceIdentifier.NodeWithValue child) {
            return Optional.fromNullable(mappedChildren.get(child));
        }

    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, LeafSetEntryNode<T>, LeafSetNode<T>> addChild(
            final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

}
