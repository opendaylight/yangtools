/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * A TreeNode capable of holding child nodes. The fact that any of the children
 * changed is tracked by the subtree version.
 */
final class ContainerNode extends AbstractTreeNode {

    private final Map<PathArgument, Optional<TreeNode>> modifChildren;
    private final Version subtreeVersion;

    protected ContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, Optional<TreeNode>> modifiedChildren, final Version subtreeVersion) {

        super(data, version);
        this.modifChildren = Preconditions.checkNotNull(modifiedChildren);
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
    }

    @Override
    public Version getSubtreeVersion() {
        return subtreeVersion;
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument key) {
        Optional<TreeNode> foundChild = modifChildren.get(key);
        return (foundChild != null ? foundChild : Optional.<TreeNode> of(this));
    }

    @Override
    public MutableTreeNode mutable() {
        return new Mutable(this);
    }

    private static final class Mutable implements MutableTreeNode {
        private final Version version;
        private final Map<PathArgument, Optional<TreeNode>> modifChildren;
        private NormalizedNode<?, ?> data;
        private Version subtreeVersion;

        private Mutable(final ContainerNode parent) {
            this.data = parent.getData();
            this.modifChildren = MapAdaptor.getDefaultInstance().takeSnapshot(parent.modifChildren);
            this.subtreeVersion = parent.getSubtreeVersion();
            this.version = parent.getVersion();
        }

        @Override
        public Optional<TreeNode> getChild(final PathArgument childPath) {
            Optional<TreeNode> foundModifChild = modifChildren.get(childPath);
            if (foundModifChild == null) {
                return Optional.of(TreeNodeFactory.createTreeNode(data, version));
            }
            return foundModifChild;
        }

        @Override
        public void setSubtreeVersion(final Version subtreeVersion) {
            this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
        }

        @Override
        public void addChild(final TreeNode child) {
            modifChildren.put(child.getIdentifier(), Optional.of(child));
        }

        @Override
        public void removeChild(final PathArgument childPath) {
            modifChildren.put(childPath, Optional.<TreeNode>absent());
        }

        @Override
        public TreeNode seal() {
            return new ContainerNode(data, version,
                    MapAdaptor.getDefaultInstance().optimize(modifChildren), subtreeVersion);
        }

        @Override
        public void setData(final NormalizedNode<?, ?> data) {
            this.data = Preconditions.checkNotNull(data);
        }
    }

    private static ContainerNode createParentContainerNode(
            final Version version, final NormalizedNode<?, ?> data) {

        final Map<PathArgument, Optional<TreeNode>> map = new HashMap<>();
        return new ContainerNode(data, version, map, version);
    }

    public static ContainerNode create(final Version version,
            final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {

        return createParentContainerNode(version, container);
    }

    public static ContainerNode create(final Version version,
            final OrderedNodeContainer<NormalizedNode<?, ?>> container) {

        return createParentContainerNode(version, container);
    }
}
