/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

final class UniqueParentMutableNode extends ForwardingMutableTreeNode {
    // FIXME: need previous vector (as unmodified, etc.)
    // FIXME: this needs to be a Multiset
    private final Map<Object, TreeNode> vectorToChild;

    UniqueParentMutableNode(final MutableTreeNode delegate, final Map<Object, TreeNode> vectorToChild) {
        super(delegate);
        this.vectorToChild = requireNonNull(vectorToChild);
    }

    @Override
    public void setData(final NormalizedNode<?, ?> data) {
        super.setData(data);
        // FIXME: reinit vectorToChild
    }

    @Override
    public void addChild(final TreeNode child) {
        super.addChild(child);
        // FIXME: update vectorToChild
    }

    @Override
    public void removeChild(final PathArgument id) {
        super.removeChild(id);
        // FIXME: update vectorToChild
    }

    @Override
    UniqueParentTreeNode seal(final TreeNode delegate) {
        return new UniqueParentTreeNode(delegate, vectorToChild);
    }
}
