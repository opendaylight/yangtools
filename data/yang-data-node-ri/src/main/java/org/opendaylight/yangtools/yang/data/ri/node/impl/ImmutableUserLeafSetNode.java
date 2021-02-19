/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableUserLeafSetNode<T>
        extends AbstractNormalizedNode<NodeIdentifier, UserLeafSetNode<?>>
        implements UserLeafSetNode<T> {
    private final Map<NodeWithValue, LeafSetEntryNode<T>> children;

    public ImmutableUserLeafSetNode(final NodeIdentifier nodeIdentifier,
            final Map<NodeWithValue, LeafSetEntryNode<T>> children) {
        super(nodeIdentifier);
        this.children = children;
    }

    @Override
    public LeafSetEntryNode<T> childByArg(final NodeWithValue child) {
        return children.get(child);
    }

    @Override
    public LeafSetEntryNode<T> getChild(final int position) {
        return Iterables.get(children.values(), position);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public Collection<LeafSetEntryNode<T>> body() {
        return UnmodifiableCollection.create(children.values());
    }

    @Override
    protected Class<UserLeafSetNode<?>> implementedType() {
        return (Class) UserLeafSetNode.class;
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    @Override
    protected boolean valueEquals(final UserLeafSetNode<?> other) {
        return children.equals(((ImmutableUserLeafSetNode<?>) other).children);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "https://github.com/spotbugs/spotbugs/issues/811") Map<NodeWithValue, LeafSetEntryNode<T>> getChildren() {
        return Collections.unmodifiableMap(children);
    }
}