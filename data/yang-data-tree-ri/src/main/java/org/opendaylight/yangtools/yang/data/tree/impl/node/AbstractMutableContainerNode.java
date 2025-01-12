/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Abstract base for container-based {@link MutableTreeNode}s. It tracks modified nodes in a map and deals with
 * correctly implementing {@link #seal()}.
 */
abstract class AbstractMutableContainerNode extends MutableTreeNode {
    private final @NonNull Version incarnation;
    private final @NonNull Version subtreeVersion;

    private Map<PathArgument, TreeNode> children;
    private @NonNull NormalizedNode data;

    AbstractMutableContainerNode(final AbstractContainerNode parent, final Version subtreeVersion,
            final Map<PathArgument, TreeNode> children) {
        data = parent.data();
        incarnation = parent.incarnation();
        this.subtreeVersion = requireNonNull(subtreeVersion);
        this.children = requireNonNull(children);
    }

    final @NonNull Version incarnation() {
        return incarnation;
    }

    final TreeNode getModifiedChild(final PathArgument child) {
        return children.get(child);
    }

    @SuppressWarnings("unchecked")
    final DistinctNodeContainer<PathArgument, NormalizedNode> getData() {
        return (DistinctNodeContainer<PathArgument, NormalizedNode>) data;
    }

    @Override
    public final TreeNode putChild(final TreeNode child) {
        return children.put(child.data().name(), child);
    }

    @Override
    public final TreeNode removeChild(final PathArgument id) {
        return children.remove(requireNonNull(id));
    }

    @Override
    public final void setData(final NormalizedNode data) {
        this.data = requireNonNull(data);
    }

    @Override
    public final AbstractContainerNode seal() {
        final AbstractContainerNode ret;

        /*
         * Decide which implementation:
         *
         * => version equals subtree version, this node has not been updated since its creation
         * => children.size() equals data child size, this node has been completely materialized and further lookups
         *    into data will not happen,
         * => more materialization can happen
         */
        if (!incarnation.equals(subtreeVersion)) {
            final var newChildren = MapAdaptor.getDefaultInstance().optimize(children);
            final int dataSize = getData().size();
            final int childrenSize = newChildren.size();
            if (dataSize != childrenSize) {
                verify(dataSize > childrenSize, "Detected %s modified children, data has only %s",
                    childrenSize, dataSize);
                ret = new LazyContainerNode(data, incarnation, newChildren, subtreeVersion);
            } else {
                ret = new MaterializedContainerNode(data, incarnation, newChildren, subtreeVersion);
            }
        } else {
            ret = new SimpleContainerNode(data, incarnation);
        }

        // This forces a NPE if this class is accessed again. Better than corruption.
        children = null;
        return ret;
    }
}
