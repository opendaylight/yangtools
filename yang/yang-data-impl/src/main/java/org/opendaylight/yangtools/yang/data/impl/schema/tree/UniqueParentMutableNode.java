/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

final class UniqueParentMutableNode extends ForwardingMutableTreeNode {
    private final @NonNull UniqueTreeNodeSupport<?> support;
    private final @NonNull UniqueParentTreeNode prev;

    private Map<UniqueValidator<?>, Multimap<Object, TreeNode>> delta = null;

    UniqueParentMutableNode(final MutableTreeNode delegate, final UniqueTreeNodeSupport support,
            final UniqueParentTreeNode prev) {
        super(delegate);
        this.support = requireNonNull(support);
        this.prev = requireNonNull(prev);
    }

    @Override
    public void setData(final NormalizedNode<?, ?> data) {
        super.setData(data);
        // FIXME: reinit delta
    }

    @Override
    public void addChild(final TreeNode child) {
        // We first create a our tree node, calculating the stored vector
        final Object vector = support.extractValues((DataContainerNode<?>) child.getData());
        final UniqueChildTreeNode childNode = new UniqueChildTreeNode(child, vector);

        // Then attempt to update child map, we want that to complete first
        super.addChild(childNode);

        // Then attempt to update indices
        // FIXME: update delta
    }

    @Override
    public void removeChild(final PathArgument id) {
        // FIXME: 7.0.0: use result instead of a get
        final Optional<? extends TreeNode> prevChild = super.getChild(id);
        super.removeChild(id);
        prevChild.ifPresent(child -> support.removeChild(ensureDelta(), child));
    }

    @Override
    public UniqueParentTreeNode seal() {
        final TreeNode delegate = sealDelegate();
        if (delegate == prev.delegate()) {
            // No changes in delegate imply no changes in index
            return prev;
        }

        final Map<UniqueValidator<?>, Multimap<Object, TreeNode>> local = delta;
        return local == null ? new UniqueParentTreeNode(delegate, prev.index())
            // Propagate changes to index only if something changed
            : support.seal(delegate, prev, local);
    }

    private @NonNull Map<UniqueValidator<?>, Multimap<Object, TreeNode>> ensureDelta() {
        var local = delta;
        if (local == null) {
            delta = local = support.allocateDelta();
        }
        return local;
    }
}
