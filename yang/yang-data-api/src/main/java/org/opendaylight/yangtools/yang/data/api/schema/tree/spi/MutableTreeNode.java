/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

/**
 * A mutable tree node. This is a transient view materialized from a pre-existing node. Modifications are isolated. Once
 * this object is {@link #seal()}ed, any interactions with it will result in undefined behavior.
 */
// FIXME: 4.0.0: Use @NonNullByDefault
public interface MutableTreeNode extends StoreTreeNode<TreeNode> {
    /**
     * Set the data component of the node.
     *
     * @param data New data component, may not be null.
     * @throws NullPointerException if {@code data} is null
     */
    void setData(NormalizedNode<?, ?> data);

    /**
     * Set the new subtree version. This is typically invoked when the user
     * has modified some of this node's children.
     *
     * @param subtreeVersion New subtree version.
     * @throws NullPointerException if {@code subtreeVersion} is null
     */
    void setSubtreeVersion(Version subtreeVersion);

    /**
     * Add a new child node. This acts as add-or-replace operation, e.g. it
     * succeeds even if a conflicting child is already present.
     *
     * @param child New child node.
     * @throws NullPointerException if {@code child} is null
     */
    void addChild(TreeNode child);

    /**
     * Remove a child node. This acts as delete-or-nothing operation, e.g. it
     * succeeds even if the corresponding child is not present.
     *
     * @param id Child identifier.
     * @throws NullPointerException if {@code id} is null
     */
    void removeChild(PathArgument id);

    /**
     * Finish node modification and return a read-only view of this node. After
     * this method is invoked, any further calls to this object's method result
     * in undefined behavior.
     *
     * @return Read-only view of this node.
     */
    @NonNull TreeNode seal();
}
