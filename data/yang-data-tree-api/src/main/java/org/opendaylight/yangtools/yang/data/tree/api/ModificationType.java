/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

/**
 * Enumeration of all possible node modification states. These are used in data tree modification context to quickly
 * assess what sort of modification the node is undergoing.
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

    /**
     * This node has appeared because it is implied by one of its children. This type is usually produced when a
     * structural container is created to host some leaf entries. It does not have an associated before-image.
     *
     * <p>
     * Its semantics is a combination of SUBTREE_MODIFIED and WRITE, depending on which context it is being interpreted.
     *
     * <p>
     * Users who track the value of the node can treat it as a WRITE. Users transforming a {@link DataTreeCandidate} to
     * operations on a {@link DataTreeModification} should interpret it as a SUBTREE_MODIFIED and examine its children.
     * This is needed to correctly deal with concurrent operations on the nodes children, as issuing a write on the
     * DataTreeModification could end up removing any leaves which have not been present at the DataTree which emitted
     * this event.
     */
    APPEARED,

    /**
     * This node has disappeared because it is no longer implied by any children. This type is usually produced when a
     * structural container is removed because it has become empty. It does not have an associated after-image.
     *
     * <p>
     * Its semantics is a combination of SUBTREE_MODIFIED and DELETE, depending on which context it is being
     * interpreted. Users who track the value of the node can treat it as a DELETE, as the container has disappeared.
     * Users transforming a {@link DataTreeCandidate} to operations on a {@link DataTreeModification} should interpret
     * it as a SUBTREE_MODIFIED and examine its children.
     *
     * <p>
     * This is needed to correctly deal with concurrent operations on the nodes children, as issuing a delete on the
     * DataTreeModification would end up removing any leaves which have not been present at the DataTree which emitted
     * this event.
     */
    DISAPPEARED,
}
