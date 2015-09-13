/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Lazily-materialized container node. Any new/modified children are tracked in a map. This map is consulted before
 * instantiating a child node from data node. Resulting node is not cached.
 */
final class LazyContainerNode extends AbstractModifiedContainerNode {
    private final Map<PathArgument, TreeNode> children;

    LazyContainerNode(final NormalizedNode<?, ?> data, final Version version, final Version subtreeVersion) {
        this(data, version, MapAdaptor.getDefaultInstance().<PathArgument, TreeNode>initialSnapshot(1), subtreeVersion);
    }

    LazyContainerNode(final NormalizedNode<?, ?> data, final Version version, final Map<PathArgument, TreeNode> children,
            final Version subtreeVersion) {
        super(data, version, subtreeVersion);
        this.children = Preconditions.checkNotNull(children);
    }

    @Override
    public MutableTreeNode mutable() {
        final Map<PathArgument, TreeNode> newChildren = MapAdaptor.getDefaultInstance().takeSnapshot(children);
        if (newChildren.size() == castData().getValue().size()) {
            return new MaterializedMutableContainerNode(this, newChildren);
        }

        return new LazyMutableContainerNode(this, newChildren);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument childId) {
        final TreeNode maybeChild = children.get(childId);
        if (maybeChild != null) {
            return Optional.of(maybeChild);
        }

        return getChildFromData(childId);
    }
}
