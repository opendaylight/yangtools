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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

/**
 * An intermediate step between a {@link UniqueVectorTreeNode} or a {@link TreeNode} and a
 * {@link UniqueVectorMutableTreeNode}.
 */
final class UniqueProxyTreeNode extends ForwardingTreeNode {
    private final @NonNull UniqueTreeNodeSupport<?> support;

    UniqueProxyTreeNode(final UniqueTreeNodeSupport<?> support, final TreeNode delegate) {
        super(delegate);
        this.support = requireNonNull(support);
    }

    @Override
    public TreeNode childByArg(final PathArgument arg) {
        return delegate().childByArg(arg);
    }

    @Override
    public MutableTreeNode mutable() {
        final TreeNode delegate = delegate();
        final UniqueVectorTreeNode vector =
            delegate instanceof UniqueVectorTreeNode uniqueDelegate ? uniqueDelegate
                : new UniqueVectorTreeNode(delegate, support.extractValues((DataContainerNode) delegate.getData()));

        return new UniqueVectorMutableTreeNode(support, vector);
    }
}
