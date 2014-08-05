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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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
    private final Map<PathArgument, TreeNode> children;
    private final Version subtreeVersion;

    protected ContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion) {
        super(data, version);
        this.children = Preconditions.checkNotNull(children);
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
    }

    @Override
    public Version getSubtreeVersion() {
        return subtreeVersion;
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument key) {
        Optional<TreeNode> explicitNode = Optional.fromNullable(children.get(key));
        if (explicitNode.isPresent()) {
            return explicitNode;
        }
        final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> castedData = (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) getData();
        Optional<NormalizedNode<?, ?>> value = castedData.getChild(key);
        if (value.isPresent()) {
            //FIXME: consider caching created Tree Nodes.
            //We are safe to not to cache them, since written Tree Nodes are in read only snapshot.
            return Optional.of(TreeNodeFactory.createTreeNode(value.get(), getVersion()));
        }
        return Optional.absent();
    }

    @Override
    public MutableTreeNode mutable() {
        return new Mutable(this);
    }

    private static final class Mutable implements MutableTreeNode {
        private final Version version;
        private Map<PathArgument, TreeNode> children;
        private NormalizedNode<?, ?> data;
        private Version subtreeVersion;

        private Mutable(final ContainerNode parent) {
            this.data = parent.getData();
            this.children = MapAdaptor.getDefaultInstance().takeSnapshot(parent.children);
            this.subtreeVersion = parent.getSubtreeVersion();
            this.version = parent.getVersion();
            materializeChildVersion();
        }

        /**
         * Traverse whole data tree and instantiate children for each data node. Set version of each MutableTreeNode
         * accordingly to version in data node.
         *
         * Use this method if TreeNode is lazy initialized.
         */
        private void materializeChildVersion() {
            Preconditions.checkState(data instanceof NormalizedNodeContainer);
            NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> castedData = (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) data;

            for(NormalizedNode<?, ?> childData : castedData.getValue()) {
                PathArgument id = childData.getIdentifier();

                if (!children.containsKey(id)) {
                    children.put(id, TreeNodeFactory.createTreeNode(childData, version));
                }
            }
        }

        @Override
        public Optional<TreeNode> getChild(final PathArgument child) {
            return Optional.fromNullable(children.get(child));
        }

        @Override
        public void setSubtreeVersion(final Version subtreeVersion) {
            this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
        }

        @Override
        public void addChild(final TreeNode child) {
            children.put(child.getIdentifier(), child);
        }

        @Override
        public void removeChild(final PathArgument id) {
            children.remove(id);
        }

        @Override
        public TreeNode seal() {
            final TreeNode ret = new ContainerNode(data, version, MapAdaptor.getDefaultInstance().optimize(children), subtreeVersion);

            // This forces a NPE if this class is accessed again. Better than corruption.
            children = null;
            return ret;
        }

        @Override
        public void setData(final NormalizedNode<?, ?> data) {
            this.data = Preconditions.checkNotNull(data);
        }
    }

    private static ContainerNode createNodeRecursively(final Version version, final NormalizedNode<?, ?> data,
        final Iterable<NormalizedNode<?, ?>> children) {

        final Map<PathArgument, TreeNode> map = new HashMap<>();
        for (NormalizedNode<?, ?> child : children) {
            map.put(child.getIdentifier(), TreeNodeFactory.createTreeNodeRecursively(child, version));
        }

        return new ContainerNode(data, version, map, version);
    }

    public static ContainerNode createNormalizedNodeRecursively(final Version version,
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {
        return createNodeRecursively(version, container, container.getValue());
    }

    public static ContainerNode createOrderedNodeRecursively(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return createNodeRecursively(version, container, container.getValue());
    }

    public static ContainerNode expandNormalizedNode(final Version version,
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {
        return createExpandedNode(version, container, container.getValue());
    }

    public static ContainerNode expandOrderedNode(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return createExpandedNode(version, container, container.getValue());
    }

    private static ContainerNode createExpandedNode(final Version version, final NormalizedNode<?, ?> data,
        final Iterable<NormalizedNode<?, ?>> children) {
        final Map<PathArgument, TreeNode> map = new HashMap<>();

        for (final NormalizedNode<?, ?> child : children) {
            if (child instanceof NormalizedNodeContainer<?, ?, ?>) {
                @SuppressWarnings("unchecked")
                NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container = (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) child;
                map.put(child.getIdentifier(), ContainerNode.createNormalizedNode(version, container));
            } else if (child instanceof OrderedNodeContainer<?>) {
                @SuppressWarnings("unchecked")
                OrderedNodeContainer<NormalizedNode<?, ?>> container = (OrderedNodeContainer<NormalizedNode<?, ?>>) child;
                map.put(child.getIdentifier(), ContainerNode.createOrderedNode(version, container));
            } else {
                map.put(child.getIdentifier(), new ValueNode(child, version));
            }
        }
        return new ContainerNode(data, version, map, version);
    }

    public static ContainerNode createNormalizedNode(final Version version,
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {
        return createNode(version, container);
    }

    public static ContainerNode createOrderedNode(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return createNode(version, container);
    }

    private static ContainerNode createNode(final Version version, final NormalizedNode<?, ?> data) {
        final Map<PathArgument, TreeNode> map = new HashMap<>();
        return new ContainerNode(data, version, map, version);
    }
}
