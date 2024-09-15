/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

/**
 * A very basic data tree node. It has a {@link #version()} (when it was last modified),
 * a {@link #subtreeVersion()} (when any of its children were modified) and some read-only data. In terms of
 * <a href="https://en.wikipedia.org/wiki/Multiversion_concurrency_control#Implementation">MVCC</a>, the former
 * corresponds to the this node's current Read Timestamp (RTS(P), where P is this node). The latter is the most recent
 * Read Timestamp in this node's accessible children.
 *
 * <p>
 * Semantic difference between these two is important when dealing with modifications involving parent/child
 * relationships and what operations can be execute concurrently without creating a data dependency conflict.
 *
 * <p>
 * A replace/delete operation cannot be applied to this node if the subtree version does not match. This mismatch
 * still allows modifications to its descendants.
 *
 * <p>
 * A mismatch in node version indicates a replacement, preventing a modification of descendants or itself.
 */
// FIXME: BUG-2399: clarify that versioning rules are not enforced for non-presence containers, as they are not
//                  considered to be data nodes.
@NonNullByDefault
public abstract sealed class TreeNode implements StoreTreeNode<TreeNode> permits BaseTreeNode, ExtendedTreeNode {
    /**
     * Get a read-only view of the underlying data.
     *
     * @return Unmodifiable view of the underlying data.
     */
    public abstract NormalizedNode data();

    /**
     * Get the data node version. This version is updated whenever the data representation of this particular node
     * changes as a result of a direct write to this node or to its parent nodes -- thus indicating that this node
     * was logically replaced.
     *
     * @return Current data node version.
     */
    public abstract Version version();

    /**
     * Get the subtree version. This version is updated whenever the data representation of this particular node
     * changes as the result of a direct or indirect child node being created, replaced or removed.
     *
     * @return Current subtree version.
     */
    public abstract Version subtreeVersion();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
