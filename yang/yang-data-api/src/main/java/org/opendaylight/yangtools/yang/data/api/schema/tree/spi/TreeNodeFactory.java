/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;

/**
 * Public entrypoint for other packages. Allows instantiating a tree node
 * with specified version.
 */
public final class TreeNodeFactory {
    private TreeNodeFactory() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
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
    @Deprecated
    private static AbstractContainerNode createNodeRecursively(final Version version, final NormalizedNode<?, ?> data,
        final Iterable<NormalizedNode<?, ?>> children) {

        final Map<PathArgument, TreeNode> map = new HashMap<>();
        for (final NormalizedNode<?, ?> child : children) {
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
     *
     */
    @Deprecated
    private static AbstractContainerNode createNormalizedNodeRecursively(final Version version,
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
    @Deprecated
    private static AbstractContainerNode createOrderedNodeRecursively(final Version version,
        final OrderedNodeContainer<NormalizedNode<?, ?>> container) {
        return createNodeRecursively(version, container, container.getValue());
    }

    /**
     * Create a new AbstractTreeNode from a data node, descending recursively as needed.
     * This method should only ever be used for new data.
     *
     * @param data data node
     * @param version data node version
     * @return new AbstractTreeNode instance, covering the data tree provided
     *
     * @deprecated Use lazy node initialization via {@link #createTreeNode(NormalizedNode, Version)}.
     */
    @Deprecated
    public static TreeNode createTreeNodeRecursively(final NormalizedNode<?, ?> data, final Version version) {
        if (data instanceof NormalizedNodeContainer<?, ?, ?>) {
            @SuppressWarnings("unchecked")
            final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container = (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) data;
            return createNormalizedNodeRecursively(version, container);

        }
        if (data instanceof OrderedNodeContainer<?>) {
            @SuppressWarnings("unchecked")
            final OrderedNodeContainer<NormalizedNode<?, ?>> container = (OrderedNodeContainer<NormalizedNode<?, ?>>) data;
            return createOrderedNodeRecursively(version, container);
        }

        return new ValueNode(data, version);
    }

    /**
     * Create a new AbstractTreeNode from a data node.
     *
     * @param data data node
     * @param version data node version
     * @return new AbstractTreeNode instance, covering the data tree provided
     */
    public static TreeNode createTreeNode(final NormalizedNode<?, ?> data, final Version version) {
        return createTreeNode(data, version, ImmutableMap.of());
    }

    /**
     * Create a new AbstractTreeNode from a data node.
     *
     * @param data data node
     * @param version data node version
     * @param indexes data node indexes
     * @return new AbstractTreeNode instance, covering the data tree provided
     */
    public static TreeNode createTreeNode(final NormalizedNode<?, ?> data, final Version version, final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes) {
        if (data instanceof NormalizedNodeContainer<?, ?, ?>) {
            @SuppressWarnings("unchecked")
            final NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>> container =
                    (NormalizedNodeContainer<?, ?, NormalizedNode<?, ?>>) data;
            return new SimpleContainerNode(container, version, indexes);
        }
        if (data instanceof OrderedNodeContainer<?>) {
            @SuppressWarnings("unchecked")
            final OrderedNodeContainer<NormalizedNode<?, ?>> container =
                    (OrderedNodeContainer<NormalizedNode<?, ?>>) data;
            return new SimpleContainerNode(container, version, indexes);
        }
        return new ValueNode(data, version, indexes);
    }
}
