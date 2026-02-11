/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Interface for creating and modifying {@link TreeNode}s.
 */
@NonNullByDefault
public interface TreeNodeAccess {
    /**
     * Create a new {@link TreeNode} from a data node.
     *
     * @param data data node
     * @param incarnation data node version
     * @return new {@link TreeNode} instance, covering the data tree provided
     */
    default TreeNode newTreeNode(final NormalizedNode data, final Version incarnation) {
        return TreeNode.of(data, incarnation);
    }

    /**
     * Get a mutable, isolated copy of a {@link TreeNode}.
     *
     * @param nextSubtreeVersion next subtree version
     * @return a new {@link MutableTreeNode}
     */
    default MutableTreeNode openTreeNode(final TreeNode treeNode, final Version nextSubtreeVersion) {
        return treeNode.toMutable(nextSubtreeVersion);
    }
}
