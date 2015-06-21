/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class LazyContainerNode extends ContainerNode {
    protected LazyContainerNode(final NormalizedNode<?, ?> data, final Version version) {
        super(data, version, version);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument key) {
        // We do not cache the instantiated node as it is dirt cheap
        final Optional<NormalizedNode<?, ?>> child = castData().getChild(key);
        if (child.isPresent()) {
            return Optional.of(TreeNodeFactory.createTreeNode(child.get(), getVersion()));
        }

        return Optional.absent();
    }

    @Override
    public MutableTreeNode mutable() {
        /*
         * We are creating a mutable view of the data, which means that the version
         * is going to probably change -- and we need to make sure any unmodified
         * children retain it.
         *
         * The simplest thing to do is to just flush the amortized work and be done
         * with it.
         */
        final Map<PathArgument, TreeNode> children = new HashMap<>();
        for (NormalizedNode<?, ?> child : castData().getValue()) {
            PathArgument id = child.getIdentifier();
            children.put(id, TreeNodeFactory.createTreeNode(child, getVersion()));
        }

        return new Mutable(this, children);
    }

    @SuppressWarnings("unchecked")
    private NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> castData() {
        return (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) getData();
    }
}
