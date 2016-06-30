/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * Abstract base for container-based {@link MutableTreeNode}s. It tracks modified nodes in a map and deals
 * with correctly implementing {@link #seal()}.
 */
abstract class AbstractMutableContainerNode implements MutableTreeNode {
    private final Version version;
    private Map<PathArgument, TreeNode> children;
    private NormalizedNode<?, ?> data;
    private Version subtreeVersion;
    private Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes;

    protected AbstractMutableContainerNode(final AbstractContainerNode parent,
            final Map<PathArgument, TreeNode> children) {
        this.data = parent.getData();
        this.indexes = MapAdaptor.getDefaultInstance().takeSnapshot(parent.getIndexes());
        this.version = parent.getVersion();
        this.subtreeVersion = parent.getSubtreeVersion();
        this.children = Preconditions.checkNotNull(children);
    }

    protected final Version getVersion() {
        return version;
    }

    protected final TreeNode getModifiedChild(final PathArgument child) {
        return children.get(child);
    }

    @SuppressWarnings("unchecked")
    protected final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> getData() {
        return (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data;
    }

    @Override
    public final void setSubtreeVersion(final Version subtreeVersion) {
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
    }

    @Override
    public final void addChild(final TreeNode child) {
        children.put(child.getIdentifier(), child);
    }

    @Override
    public final void addChild(final TreeNode child, final IndexStrategy strategy) {
        addChild(child);
        strategy.updateIndexes(indexes, child.getData());
    }

    @Override
    public final void removeChild(final PathArgument id) {
        children.remove(id);
    }

    @Override
    public final void removeChild(final PathArgument id, final IndexStrategy strategy) {
        removeChild(id);
        strategy.removeFromIndexes(indexes, id);
    }

    @Override
    public final void setData(final NormalizedNode<?, ?> data) {
        this.data = Preconditions.checkNotNull(data);
    }

    @Override
    public final TreeNode seal() {
        final TreeNode ret;

        /*
         * Decide which implementation:
         *
         * => version equals subtree version, this node has not been updated since its creation
         * => children.size() equals data child size, this node has been completely materialized and further lookups
         *    into data will not happen,
         * => more materialization can happen
         */
        sealMutableIndexes();
        if (!version.equals(subtreeVersion)) {
            final Map<PathArgument, TreeNode> newChildren = MapAdaptor.getDefaultInstance().optimize(children);
            final int dataSize = getData().getValue().size();
            if (dataSize != newChildren.size()) {
                Verify.verify(dataSize > newChildren.size(), "Detected %s modified children, data has only %s",
                        newChildren.size(), dataSize);
                ret = new LazyContainerNode(data, version, newChildren, subtreeVersion, indexes);
            } else {
                ret = new MaterializedContainerNode(data, version, newChildren, subtreeVersion, indexes);
            }
        } else {
            ret = new SimpleContainerNode(data, version, indexes);
        }

        // This forces a NPE if this class is accessed again. Better than
        // corruption.
        children = null;
        return ret;
    }

    @Override
    public void buildIndexes(final IndexStrategy strategy) {
        this.indexes = strategy.createIndexesFromData(data);
    }

    private void sealMutableIndexes() {
        for (final Entry<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexEntry : indexes.entrySet()) {
            final TreeNodeIndex<?, ?> treeNodeIndex = treeNodeIndexEntry.getValue();
            if (treeNodeIndex instanceof MutableTreeNodeIndex) {
                final TreeNodeIndex<?, ?> sealed = ((MutableTreeNodeIndex<?, ?>) treeNodeIndex).seal();
                indexes.put(treeNodeIndexEntry.getKey(), sealed);
            }
        }
    }
}
