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
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

abstract class AbstractMutableContainerNode implements MutableTreeNode {
    private final Version version;
    private Map<PathArgument, TreeNode> children;
    private NormalizedNode<?, ?> data;
    private Version subtreeVersion;

    AbstractMutableContainerNode(final AbstractContainerNode parent, final Map<PathArgument, TreeNode> children) {
        this.data = parent.getData();
        this.version = parent.getVersion();
        this.subtreeVersion = parent.getSubtreeVersion();
        this.children = Preconditions.checkNotNull(children);
    }

    protected final TreeNode getModifiedChild(final PathArgument child) {
        return children.get(child);
    }

    protected final Version getVersion() {
        return version;
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
    public final void removeChild(final PathArgument id) {
        children.remove(id);
    }

    @Override
    public final void setData(final NormalizedNode<?, ?> data) {
        this.data = Preconditions.checkNotNull(data);
    }

    @Override
    public final TreeNode seal() {
        final TreeNode ret;
        if (!version.equals(subtreeVersion)) {
            final Map<PathArgument, TreeNode> newChildren = MapAdaptor.getDefaultInstance().optimize(children);
            final int dataSize = getData().getValue().size();
            if (dataSize == newChildren.size()) {
                ret = new MaterializedContainerNode(data, version, newChildren, subtreeVersion);
            } else {
                Verify.verify(dataSize >= newChildren.size(), "Detected %s modified children, data has only %s",
                        dataSize, newChildren.size());
                ret = new LazyContainerNode(data, version, newChildren, subtreeVersion);
            }
        } else {
            ret = new UnmaterializedContainerNode(data, version);
        }

        // This forces a NPE if this class is accessed again. Better than corruption.
        children = null;
        return ret;
    }
}