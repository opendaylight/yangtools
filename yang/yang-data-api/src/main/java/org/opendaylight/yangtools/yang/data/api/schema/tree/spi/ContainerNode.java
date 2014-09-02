/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;

/**
 * A TreeNode capable of holding child nodes. The fact that any of the children
 * changed is tracked by the subtree version.
 */
abstract class ContainerNode extends AbstractTreeNode {
    private final Version subtreeVersion;

    protected ContainerNode(final NormalizedNode<?, ?> data, final Version version, final Version subtreeVersion) {
        super(data, version);
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
    }

    @Override
    public final Version getSubtreeVersion() {
        return subtreeVersion;
    }

    protected static final class Mutable implements MutableTreeNode {
        private final Version version;
        private Map<PathArgument, TreeNode> children;
        private NormalizedNode<?, ?> data;
        private Version subtreeVersion;

        Mutable(final ContainerNode parent, final Map<PathArgument, TreeNode> children) {
            this.data = parent.getData();
            this.version = parent.getVersion();
            this.subtreeVersion = parent.getSubtreeVersion();
            this.children = Preconditions.checkNotNull(children);
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
            final TreeNode ret = new MaterializedContainerNode(data, version, MapAdaptor.getDefaultInstance().optimize(children), subtreeVersion);

            // This forces a NPE if this class is accessed again. Better than corruption.
            children = null;
            return ret;
        }

        @Override
        public void setData(final NormalizedNode<?, ?> data) {
            this.data = Preconditions.checkNotNull(data);
        }
    }

    /**
     * Method creates and returns Container root Node and whole subtree for each child node specified in children nodes.
     * <br>
     * Reason why is method used recursively is that for each child in children nodes there is call to
     * {@link TreeNodeFactory#createTreeNodeRecursively}. Each call to <code>createTreeNodeRecursively</code>
     * calls either {@link #createNormalizedNodeRecursively} or {@link #createOrderedNodeRecursively}
     * which depends on type of child node.
     * <br> The root node that is returned holds reference to data node and whole subtree of children also containing references
     * to data nodes.
     *
     * @param version version of indexed data
     * @param data reference to data node
     * @param children direct children of root node that is being created
     * @return Root node with reference to data node and whole subtree of child nodes
     */
    private static ContainerNode createNodeRecursively(final Version version, final NormalizedNode<?, ?> data,
        final Iterable<NormalizedNode<?, ?>> children) {

        final Map<PathArgument, TreeNode> map = new HashMap<>();
        for (NormalizedNode<?, ?> child : children) {
            map.put(child.getIdentifier(), TreeNodeFactory.createTreeNodeRecursively(child, version));
        }

        return new MaterializedContainerNode(data, version, map, version);
    }

    /**
     * Method creates and returns Normalized Node Container as root and recursively creates whole subtree
     * from all of the container child iterables stored in {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer#getValue()}
     * <br>
     * The reason why is this method called recursively is that in background method calls {@link TreeNodeFactory#createTreeNodeRecursively}
     * for each child stored in NormalizedNode and after each child is created the method calls again {@link #createNormalizedNodeRecursively} method
     * until all of the children are resolved.
     *
     * @param version version of indexed data
     * @param container Normalized Node Container
     * @return Normalized Node Container as root and all whole subtree created from container iterables.
     */
    public static ContainerNode createNormalizedNodeRecursively(final Version version,
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {
        return createNodeRecursively(version, container, container.getValue());
    }

    /**
     * Method creates and returns Ordered Node Container as root and recursively creates whole subtree
     * from all of the container child iterables stored in {@link org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer#getValue()}
     * <br>
     * The reason why is this method called recursively is that in background method calls {@link TreeNodeFactory#createTreeNodeRecursively}
     * for each child stored in NormalizedNode and after each child is created the method calls again {@link #createNormalizedNodeRecursively} method
     * until all of the children are resolved.
     *
     * @param version version of indexed data
     * @param container Ordered Node Container
     * @return Normalized Ordered Container as root and all whole subtree created from container iterables.
     */
    public static ContainerNode createOrderedNodeRecursively(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return createNodeRecursively(version, container, container.getValue());
    }

    /**
     * Creates and returns single instance of Normalized Node Container with provided version and data reference stored in NormalizedNodeContainer.
     *
     * @param version version of indexed data
     * @param container Normalized Node Container
     * @return single instance of Normalized node with provided version and data reference stored in NormalizedNodeContainer
     */
    public static ContainerNode createNormalizedNode(final Version version,
        final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container) {
        return new LazyContainerNode(container, version);
    }

    /**
     * Creates and returns single instance of Ordered Node Container with provided version and data reference stored in OrderedNodeContainer.
     *
     * @param version version of indexed data
     * @param container Ordered Node Container
     * @return single instance of Ordered Node Container with provided version and data reference stored in OrderedNodeContainer.
     */
    public static ContainerNode createOrderedNode(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return new LazyContainerNode(container, version);
    }
}
