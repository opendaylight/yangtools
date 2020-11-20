/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

final class UniqueIndexMutableNode extends AttachedMutableTreeNode {
    private final @NonNull UniqueTreeNodeSupport<?> support;
    private final @NonNull UniqueIndexTreeNode prev;

    private Map<TreeNode, Boolean> delta = null;

    UniqueIndexMutableNode(final MutableTreeNode delegate, final UniqueTreeNodeSupport<?> support,
            final UniqueIndexTreeNode prev) {
        super(delegate);
        this.support = requireNonNull(support);
        this.prev = requireNonNull(prev);
    }

    @Override
    public TreeNode putChild(final TreeNode child) {
        verify(child instanceof UniqueVectorTreeNode, "Unexpected child node %s", child);
        final UniqueVectorTreeNode vector = (UniqueVectorTreeNode) child;

        // Attempt to update child map, we want that to complete first
        final TreeNode prevChild = super.putChild(vector);

        // Then update the delta
        final Map<TreeNode, Boolean> local = ensureDelta();
        if (prevChild != null) {
            local.put(prevChild, Boolean.FALSE);
        }
        local.put(vector, Boolean.TRUE);
        return prevChild;
    }

    @Override
    public TreeNode removeChild(final PathArgument id) {
        final TreeNode child = super.removeChild(id);
        if (child != null) {
            ensureDelta().put(child, Boolean.FALSE);
        }
        return child;
    }

    @Override
    public UniqueIndexTreeNode seal() {
        final TreeNode delegate = sealDelegate();
        if (delegate == prev.delegate()) {
            // No changes in delegate imply no changes in index
            return prev;
        }

        final Map<TreeNode, Boolean> local = delta;
        return local == null ? new UniqueIndexTreeNode(delegate, prev.index())
            // Propagate changes to index only if something changed
            : support.seal(delegate, prev, local);
    }

    @Override
    TreeNode wrapChild(final TreeNode child) {
        return new UniqueProxyTreeNode(support, child);
    }

    private @NonNull Map<TreeNode, Boolean> ensureDelta() {
        var local = delta;
        if (local == null) {
            delta = local = new HashMap<>();
        }
        return local;
    }
}
