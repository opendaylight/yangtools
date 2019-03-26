/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

/**
 * A very basic data tree node. It has a version (when it was last modified), a subtree version (when any of its
 * children were modified) and some read-only data.
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
public interface TreeNode extends Identifiable<PathArgument>, StoreTreeNode<TreeNode> {
    /**
     * Get the data node version. This version is updated whenever the data representation of this particular node
     * changes as a result of a direct write to this node or to its parent nodes -- thus indicating that this node
     * was logically replaced.
     *
     * @return Current data node version.
     */
    Version getVersion();

    /**
     * Get the subtree version. This version is updated whenever the data representation of this particular node
     * changes as the result of a direct or indirect child node being created, replaced or removed.
     *
     * @return Current subtree version.
     */
    Version getSubtreeVersion();

    /**
     * Get a read-only view of the underlying data.
     *
     * @return Unmodifiable view of the underlying data.
     */
    NormalizedNode<?, ?> getData();

    /**
     * Get a mutable, isolated copy of the node.
     *
     * @return Mutable copy
     */
    MutableTreeNode mutable();
}
