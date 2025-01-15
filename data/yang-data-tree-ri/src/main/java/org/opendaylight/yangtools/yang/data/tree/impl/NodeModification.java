/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

/**
 * Internal interface representing a modification action of a particular node. It is used by the validation code to
 * allow for a read-only view of the modification tree as we should never modify that during validation.
 */
abstract sealed class NodeModification implements Identifiable<PathArgument> permits ModifiedNode {
    /**
     * Get the type of modification.
     *
     * @return Operation type.
     */
    abstract @NonNull LogicalOperation getOperation();

    /**
     * Return the value which was written to this node. The returned object is only valid for
     * {@link LogicalOperation#MERGE} and {@link LogicalOperation#WRITE}.
     * operations. It should only be consulted when this modification is going to end up being
     * {@link ModificationType#WRITE}.
     *
     * @return Currently-written value
     */
    abstract @NonNull NormalizedNode getValue();

    /**
     * Get the original tree node to which the modification is to be applied.
     *
     * @return The original node, or {@code null} if the node is a new node.
     */
    abstract @Nullable TreeNode original();

    /**
     * Get a read-only view of children nodes.
     *
     * @return Collection of all children nodes.
     */
    abstract Collection<? extends NodeModification> getChildren();

    /**
     * A shortcut to {@code getChildren().isEmpty()}.
     *
     * @return {@code} if {@link #getChildren()} is empty.
     */
    abstract boolean isEmpty();
}
