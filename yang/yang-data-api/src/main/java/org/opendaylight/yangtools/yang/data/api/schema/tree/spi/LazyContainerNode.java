/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Collection;
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

    // Assumes default load factor of 0.75 and capacity of 16.
    private static <K, V> Map<K, V> allocateMap(final int hint) {
        switch (hint) {
        case 0:
        case 1:
            // Zero does not matter, but will be kept small if it is touched in the future
            return new HashMap<>(1);
        case 2:
            // Two entries, may end up being grown to 4
            return new HashMap<>(2);
        case 3:
            // 4 * 0.75 = 3
            return new HashMap<>(4);
        case 4:
        case 5:
        case 6:
            // 8 * 0.75 = 6
            return new HashMap<>(8);
        default:
            // No savings, defer to Guava
            return Maps.newHashMapWithExpectedSize(hint);
        }
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
        final Collection<NormalizedNode<?, ?>> oldChildren = castData().getValue();

        // Use a proper sizing hint here, as the default size can suck for both extremely large and extremely small
        // collections. For the large ones we end up rehashing the table, for small ones we end up using more space
        // than necessary.
        final Map<PathArgument, TreeNode> children = allocateMap(oldChildren.size());
        for (NormalizedNode<?, ?> child : oldChildren) {
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
