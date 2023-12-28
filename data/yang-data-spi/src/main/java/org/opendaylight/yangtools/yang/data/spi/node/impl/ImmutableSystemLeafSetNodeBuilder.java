/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;

public final class ImmutableSystemLeafSetNodeBuilder<T> implements SystemLeafSetNode.Builder<T> {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeWithValue<?>, LeafSetEntryNode<T>> value;

    private NodeIdentifier nodeIdentifier;

    public ImmutableSystemLeafSetNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    public ImmutableSystemLeafSetNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    private ImmutableSystemLeafSetNodeBuilder(final ImmutableSystemLeafSetNode<T> node) {
        nodeIdentifier = node.name();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(node.children);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> create(final SystemLeafSetNode<T> node) {
        if (node instanceof ImmutableSystemLeafSetNode) {
            return new ImmutableSystemLeafSetNodeBuilder<>((ImmutableSystemLeafSetNode<T>) node);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        value.remove(key);
        return this;
    }

    @Override
    public SystemLeafSetNode<T> build() {
        return new ImmutableSystemLeafSetNode<>(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> withValue(final Collection<LeafSetEntryNode<T>> withValue) {
        for (var leafSetEntry : withValue) {
            withChild(leafSetEntry);
        }
        return this;
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        return withChild(ImmutableLeafSetEntryNode.of(new NodeWithValue<>(nodeIdentifier.getNodeType(), childValue)));
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public ImmutableSystemLeafSetNodeBuilder<T> removeChild(final PathArgument key) {
        return withoutChild(key);
    }
}
