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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

final class UniqueParentMutableNode extends ForwardingMutableTreeNode {
    // FIXME: need previous vector (as unmodified, etc.)
    private final Multimap<Object, TreeNode> vectorToChild;
    private final UniqueTreeNodeSupport support;

    UniqueParentMutableNode(final MutableTreeNode delegate, final UniqueTreeNodeSupport support,
            final Map<Object, TreeNode> vectorToChild) {
        super(delegate);
        this.support = requireNonNull(support);
        this.vectorToChild = requireNonNull(vectorToChild);
    }

    @Override
    public void setData(final NormalizedNode<?, ?> data) {
        super.setData(data);
        // FIXME: reinit vectorToChild
    }

    @Override
    public void addChild(final TreeNode child) {
        // We first create a our tree node, calculating the stored vector
        // FIXME: initialize vector
        final Object vector = null;
        final UniqueChildTreeNode childNode = new UniqueChildTreeNode(child, vector);

        // Then attempt to update child map, we want that to complete first
        super.addChild(childNode);

        // Then attempt to update indices
        // FIXME: update vectorToChild
    }

    @Override
    public void removeChild(final PathArgument id) {
        // FIXME: 7.0.0: use result instead of a get
        final Optional<? extends TreeNode> prev = super.getChild(id);
        super.removeChild(id);
        prev.ifPresent(child -> vectorToChild.remove(((UniqueChildTreeNode) child).vector(), child));
    }

    @Override
    UniqueParentTreeNode seal(final TreeNode delegate) {
        return support.seal(delegate, vectorToChild.asMap());
    }
}
