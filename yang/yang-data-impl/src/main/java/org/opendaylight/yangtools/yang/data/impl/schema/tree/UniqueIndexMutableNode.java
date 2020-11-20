/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

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
    public void addChild(final TreeNode child) {
        verify(child instanceof UniqueVectorTreeNode, "Unexpected child node %s", child);
        final UniqueVectorTreeNode vector = (UniqueVectorTreeNode) child;

        // Attempt to update child map, we want that to complete first
        // FIXME: 7.0.0: use result instead of a get
        final Optional<? extends TreeNode> prevChild = super.getChild(child.getIdentifier());
        super.addChild(vector);

        // Then update the delta
        final Map<TreeNode, Boolean> local = ensureDelta();
        prevChild.ifPresent(oldChild -> local.put(oldChild, Boolean.FALSE));
        local.put(vector, Boolean.TRUE);
    }

    @Override
    public void removeChild(final PathArgument id) {
        // FIXME: 7.0.0: use result instead of a get
        final Optional<? extends TreeNode> prevChild = super.getChild(id);
        super.removeChild(id);
        prevChild.ifPresent(child -> ensureDelta().put(child, Boolean.FALSE));
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
