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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;

public final class ImmutableUserLeafSetNodeBuilder<T> implements UserLeafSetNode.Builder<T> {
    private Map<NodeWithValue<T>, LeafSetEntryNode<T>> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    public ImmutableUserLeafSetNodeBuilder() {
        value = new LinkedHashMap<>();
        dirty = false;
    }

    public ImmutableUserLeafSetNodeBuilder(final int sizeHint) {
        value = Maps.newLinkedHashMapWithExpectedSize(sizeHint);
        dirty = false;
    }

    ImmutableUserLeafSetNodeBuilder(final ImmutableUserLeafSetNode<T> node) {
        nodeIdentifier = node.name();
        value = Collections.unmodifiableMap(node.children);
        dirty = true;
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> create(final UserLeafSetNode<T> node) {
        if (node instanceof ImmutableUserLeafSetNode<?>) {
            return new ImmutableUserLeafSetNodeBuilder<>((ImmutableUserLeafSetNode<T>) node);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        checkDirty();
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        checkDirty();
        value.remove(key);
        return this;
    }

    @Override
    public UserLeafSetNode<T> build() {
        dirty = true;
        return new ImmutableUserLeafSetNode<>(nodeIdentifier, value);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withValue(final Collection<LeafSetEntryNode<T>> withValue) {
        checkDirty();
        for (final LeafSetEntryNode<T> leafSetEntry : withValue) {
            withChild(leafSetEntry);
        }
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        return withChild(ImmutableLeafSetEntryNode.of(new NodeWithValue<>(nodeIdentifier.getNodeType(), childValue)));
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> removeChild(final PathArgument key) {
        return withoutChild(key);
    }
}
