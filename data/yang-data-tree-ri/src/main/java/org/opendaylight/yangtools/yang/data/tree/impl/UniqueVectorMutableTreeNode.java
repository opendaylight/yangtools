/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

/**
 * This is a mutable view of a {@link UniqueVectorTreeNode}. It does nothing magical with children. On seal it acquires
 * an appropriate vector from data. It is a constituent tree node of a {@link UniqueIndexTreeNode}.
 */
final class UniqueVectorMutableTreeNode extends AttachedMutableTreeNode {
    private final @NonNull UniqueTreeNodeSupport<?> support;
    private final @NonNull UniqueVectorTreeNode prev;

    UniqueVectorMutableTreeNode(final UniqueTreeNodeSupport<?> support, final UniqueVectorTreeNode prev) {
        super(prev.delegate().mutable());
        this.support = requireNonNull(support);
        this.prev = prev;
    }

    @Override
    public UniqueVectorTreeNode seal() {
        final TreeNode sealed = sealDelegate();
        return sealed == prev.delegate() ? prev
            : new UniqueVectorTreeNode(sealed, support.extractValues(sealed, prev.vector()));
    }
}
