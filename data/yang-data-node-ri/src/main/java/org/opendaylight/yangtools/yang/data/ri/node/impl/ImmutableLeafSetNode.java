/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;

public final class ImmutableLeafSetNode<T>
        extends AbstractImmutableNormalizedValueNode<NodeIdentifier, SystemLeafSetNode<?>,
            Collection<@NonNull LeafSetEntryNode<T>>>
        implements SystemLeafSetNode<T> {

    final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children;

    public ImmutableLeafSetNode(final NodeIdentifier nodeIdentifier,
            final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children) {
        super(nodeIdentifier, UnmodifiableCollection.create(children.values()));
        this.children = children;
    }

    @Override
    public LeafSetEntryNode<T> childByArg(final NodeWithValue<?> child) {
        return children.get(child);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    protected Class<SystemLeafSetNode<?>> implementedType() {
        return (Class) SystemLeafSetNode.class;
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final SystemLeafSetNode<?> other) {
        return children.equals(((ImmutableLeafSetNode<?>) other).children);
    }
}