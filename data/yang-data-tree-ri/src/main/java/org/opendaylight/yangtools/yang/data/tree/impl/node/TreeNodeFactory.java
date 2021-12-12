/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer;

/**
 * Public entrypoint for other packages. Allows instantiating a tree node
 * with specified version.
 */
public final class TreeNodeFactory {
    private TreeNodeFactory() {
        // Hidden on purpose
    }

    /**
     * Create a new AbstractTreeNode from a data node.
     *
     * @param data data node
     * @param version data node version
     * @return new AbstractTreeNode instance, covering the data tree provided
     */
    public static TreeNode createTreeNode(final NormalizedNode data, final Version version) {
        if (data instanceof DistinctNodeContainer) {
            @SuppressWarnings("unchecked")
            final DistinctNodeContainer<?, NormalizedNode> container = (DistinctNodeContainer<?, NormalizedNode>) data;
            return new SimpleContainerNode(container, version);
        } else if (data instanceof OrderedNodeContainer) {
            return new SimpleContainerNode(data, version);
        } else {
            return new ValueNode(data, version);
        }
    }
}
