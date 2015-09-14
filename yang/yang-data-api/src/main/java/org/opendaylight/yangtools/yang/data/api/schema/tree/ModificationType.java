/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

/**
 * Enumeration of all possible node modification states. These are used in
 * data tree modification context to quickly assess what sort of modification
 * the node is undergoing.
 */
public enum ModificationType {
    /**
     * Node is currently unmodified.
     */
    UNMODIFIED,

    /**
     * A child node, either direct or indirect, has been modified. This means
     * that the data representation of this node has potentially changed.
     */
    SUBTREE_MODIFIED,

    /**
     * This node has been placed into the tree, potentially completely replacing
     * pre-existing contents.
     */
    WRITE,

    /**
     * This node has been deleted along with any of its child nodes.
     */
    DELETE,
}
