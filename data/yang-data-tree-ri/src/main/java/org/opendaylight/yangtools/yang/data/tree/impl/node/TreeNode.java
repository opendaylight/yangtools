/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

/**
 * A very basic data tree node. It has a {@link #incarnation()} (when it was last modified),
 * a {@link #subtreeVersion()} (when any of its children were modified) and some read-only data. In terms of
 * <a href="https://en.wikipedia.org/wiki/Multiversion_concurrency_control#Implementation">MVCC</a>, the former
 * corresponds to the this node's current Read Timestamp (RTS(P), where P is this node). The latter is the most recent
 * Read Timestamp in this node's accessible children.
 *
 * <p>Semantic difference between these two is important when dealing with modifications involving parent/child
 * relationships and what operations can be execute concurrently without creating a data dependency conflict.
 *
 * <p>A replace/delete operation cannot be applied to this node if the subtree version does not match. This mismatch
 * still allows modifications to its descendants.
 *
 * <p>A mismatch in node version indicates a replacement, preventing a modification of descendants or itself.
 */
// FIXME: BUG-2399: clarify that versioning rules are not enforced for non-presence containers, as they are not
//                  considered to be data nodes.
@NonNullByDefault
public abstract class TreeNode implements StoreTreeNode<TreeNode> {
    private final NormalizedNode data;
    private final Version incarnation;

    TreeNode(final NormalizedNode data, final Version incarnation) {
        this.data = requireNonNull(data);
        this.incarnation = requireNonNull(incarnation);
    }

    /**
     * Create a new AbstractTreeNode from a data node.
     *
     * @param data data node
     * @param incarnation data node version
     * @return new AbstractTreeNode instance, covering the data tree provided
     */
    public static final TreeNode of(final NormalizedNode data, final Version incarnation) {
        return switch (data) {
            case DistinctNodeContainer<?, ?> distinct -> {
                @SuppressWarnings("unchecked")
                final var container = (DistinctNodeContainer<?, NormalizedNode>) data;
                yield new SimpleContainerNode(container, incarnation);
            }
            case OrderedNodeContainer<?> ordered -> new SimpleContainerNode(ordered, incarnation);
            default -> new ValueNode(data, incarnation);
        };
    }

    /**
     * Get a read-only view of the underlying data.
     *
     * @return Unmodifiable view of the underlying data.
     */
    public final NormalizedNode data() {
        return data;
    }

    /**
     * Get the data node incarnation. This version is updated whenever the data representation of this particular node
     * changes as a result of a direct write to this node or to its parent nodes -- thus indicating that this node
     * was logically replaced.
     *
     * @return data node incarnation
     */
    public final Version incarnation() {
        return incarnation;
    }

    /**
     * Get the subtree version. This version is updated whenever the data representation of this particular node
     * changes as the result of a direct or indirect child node being created, replaced or removed.
     *
     * @return Current subtree version.
     */
    public abstract Version subtreeVersion();

    /**
     * Get a mutable, isolated copy of the node.
     *
     * @param nextSubtreeVersion next subtree version
     * @return Mutable copy
     * @throws NullPointerException if {@code nextSubtreeVersion} is {@code null}
     */
    public abstract MutableTreeNode toMutable(Version nextSubtreeVersion);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("version", incarnation)).toString();
    }

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);
}
