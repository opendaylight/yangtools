/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractUserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;

final class ImmutableUserLeafSetNode<T> extends AbstractUserLeafSetNode<T> {
    private final @NonNull NodeIdentifier name;
    final @NonNull Map<NodeWithValue<T>, LeafSetEntryNode<T>> children;

    ImmutableUserLeafSetNode(final NodeIdentifier name, final Map<NodeWithValue<T>, LeafSetEntryNode<T>> children) {
        this.name = requireNonNull(name);
        this.children = requireNonNull(children);
    }

    @Override
    public NodeIdentifier name() {
        return name;
    }

    @Override
    public LeafSetEntryNode<T> childByArg(final NodeWithValue<?> child) {
        return children.get(child);
    }

    @Override
    public LeafSetEntryNode<T> childAt(final int position) {
        return Iterables.get(children.values(), position);
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
    protected boolean valueEquals(final UserLeafSetNode<T> other) {
        return other instanceof ImmutableUserLeafSetNode<?> immutableOther
            ? children.equals(immutableOther.children)
                // Note: performs a size() check first
                : Iterables.elementsEqual(children.values(), other.body());
    }

    @Override
    protected Collection<LeafSetEntryNode<T>> value() {
        return children.values();
    }

    @Override
    protected Collection<LeafSetEntryNode<T>> wrappedValue() {
        return UnmodifiableCollection.create(value());
    }
}