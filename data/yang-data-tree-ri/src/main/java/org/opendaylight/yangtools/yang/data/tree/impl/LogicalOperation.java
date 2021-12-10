/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * Enumeration of all possible node modification states. These are used in
 * data tree modification context to quickly assess what sort of modification
 * the node is undergoing. This is a superset of {@link ModificationType}:
 * where this type represents a logical operation, {@link ModificationType}
 * represents a physical operation.
 */
enum LogicalOperation {
    /**
     * Node is currently unmodified.
     */
    NONE,

    /**
     * A child node, either direct or indirect, has been modified. This means
     * that the data representation of this node has potentially changed.
     */
    TOUCH,

    /**
     * This node has been placed into the tree, potentially completely replacing
     * pre-existing contents.
     */
    WRITE,

    /**
     * This node has been deleted along with any of its child nodes.
     */
    DELETE,

    /**
     * Node has been written into the tree, but instead of replacing pre-existing
     * contents, it has been merged. This means that any incoming nodes which
     * were present in the tree have been replaced, but their child nodes have
     * been retained.
     */
    MERGE,
}
