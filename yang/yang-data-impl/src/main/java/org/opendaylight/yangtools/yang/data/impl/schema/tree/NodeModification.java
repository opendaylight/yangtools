/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * Internal interface representing a modification action of a particular node.
 * It is used by the validation code to allow for a read-only view of the
 * modification tree as we should never modify that during validation.
 */
abstract class NodeModification implements Identifiable<PathArgument> {
    /**
     * Get the type of modification.
     *
     * @return Operation type.
     */
    abstract LogicalOperation getOperation();

    /**
     * Get the original tree node to which the modification is to be applied.
     *
     * @return The original node, or {@link Optional#absent()} if the node is
     *         a new node.
     */
    abstract Optional<TreeNode> getOriginal();

    /**
     * Get a read-only view of children nodes.
     *
     * @return Collection of all children nodes.
     */
    abstract Collection<? extends NodeModification> getChildren();
}
