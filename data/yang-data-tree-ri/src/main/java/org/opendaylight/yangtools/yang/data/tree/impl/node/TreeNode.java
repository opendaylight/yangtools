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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;
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
public abstract sealed class TreeNode implements StoreTreeNode<TreeNode> permits RawTreeNode, DecoratingTreeNode {
    /**
     * Create a new AbstractTreeNode from a data node.
     *
     * @param data data node
     * @param version data node version
     * @return new AbstractTreeNode instance, covering the data tree provided
     */
    public static final TreeNode of(final NormalizedNode data, final Version version) {
        return switch (data) {
            case DistinctNodeContainer<?, ?> distinct -> {
                @SuppressWarnings("unchecked")
                final var container = (DistinctNodeContainer<?, NormalizedNode>) data;
                yield new SimpleContainerNode(container, version);
            }
            case OrderedNodeContainer<?> ordered -> new SimpleContainerNode(ordered, version);
            default -> new ValueNode(data, version);
        };
    }

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
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("version", version());
    }
}
