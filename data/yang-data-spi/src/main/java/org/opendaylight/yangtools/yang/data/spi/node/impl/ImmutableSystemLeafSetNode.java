/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractSystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;

final class ImmutableSystemLeafSetNode<T> extends AbstractSystemLeafSetNode<T> {
    private final @NonNull NodeIdentifier name;
    final @NonNull Map<NodeWithValue<?>, LeafSetEntryNode<T>> children;

    ImmutableSystemLeafSetNode(final NodeIdentifier name, final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children) {
        this.name = requireNonNull(name);
        this.children = requireNonNull(children);
    }

    @Override
    public NodeIdentifier name() {
        body();
        return name;
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
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected Collection<LeafSetEntryNode<T>> value() {
        return children.values();
    }

    @Override
    protected Collection<LeafSetEntryNode<T>> wrappedValue() {
        return UnmodifiableCollection.create(value());
    }

    @Override
    protected boolean valueEquals(final SystemLeafSetNode<T> other) {
        if (other instanceof ImmutableSystemLeafSetNode<?> otherImmutable) {
            return children.equals(otherImmutable.children);
        }
        if (size() != other.size()) {
            return false;
        }
        for (var child : children.values()) {
            if (!child.equals(other.childByArg(child.name()))) {
                return false;
            }
        }
        return true;
    }
}