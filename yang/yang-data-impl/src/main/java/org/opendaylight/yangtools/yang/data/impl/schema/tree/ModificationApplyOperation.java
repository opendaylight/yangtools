/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * Operation responsible for applying {@link ModifiedNode} on tree.
 *
 * Operation is composite - operation on top level node consists of
 * suboperations on child nodes. This allows to walk operation hierarchy and
 * invoke suboperations independently.
 *
 * <b>Implementation notes</b>
 * <ul>
 * <li>
 * Implementations MUST expose all nested suboperations which operates on child
 * nodes expose via {@link #getChild(PathArgument)} method.
 * <li>Same suboperations SHOULD be used when invoked via
 * {@link #apply(ModifiedNode, Optional, Version)} if applicable.
 *
 *
 * Hierarchical composite operation which is responsible for applying
 * modification on particular subtree and creating updated subtree
 */
abstract class ModificationApplyOperation implements StoreTreeNode<ModificationApplyOperation> {
    /**
     *
     * Implementation of this operation must be stateless and must not change
     * state of this object.
     *
     * @param modification
     *            NodeModification to be applied
     * @param storeMeta
     *            Store Metadata Node on which NodeModification should be
     *            applied
     * @param version New subtree version of parent node
     * @throws IllegalArgumentException
     *             If it is not possible to apply Operation on provided Metadata
     *             node
     * @return new {@link TreeNode} if operation resulted in updating
     *         node, {@link Optional#absent()} if {@link ModifiedNode}
     *         resulted in deletion of this node.
     */
    abstract Optional<TreeNode> apply(ModifiedNode modification, Optional<TreeNode> storeMeta, Version version);

    /**
     * Checks if provided node modification could be applied to current metadata node.
     *
     * @param modification Modification
     * @param current Metadata Node to which modification should be applied
     * @param version
     * @throws DataValidationFailedException if the modification is not applicable
     */
   abstract void checkApplicable(YangInstanceIdentifier path, NodeModification modification, Optional<TreeNode> current, Version version) throws DataValidationFailedException;

    /**
     *
     * Performs structural verification of NodeModification, such as writen values / types uses
     * right structural elements.
     *
     * @param modification data to be verified.
     * @param verifyChildren true if structure verification should be run against children.
     * @throws IllegalArgumentException If provided NodeModification does not adhere to the
     *         structure.
     */
    abstract void verifyStructure(NormalizedNode<?, ?> modification, boolean verifyChildren)
            throws IllegalArgumentException;

    /**
     * Return the tracking policy for this node's children.
     *
     * @return Tracking policy, may not be null.
     */
    abstract ChildTrackingPolicy getChildPolicy();

    /**
     * Stage a merge operation into a {@link ModifiedNode} such that it will be processed correctly by
     * {@link #apply(ModifiedNode, Optional, Version)}. This method is the context which is introducing this operation,
     * and so any overheads are charged to whoever is in control of the access pattern.
     *
     * @param modification Original modification node
     * @param value Value which should be merge into the modification
     * @param version Data version as carried in the containing {@link InMemoryDataTreeModification}
     */
    abstract void mergeIntoModifiedNode(ModifiedNode modification, NormalizedNode<?, ?> value, Version version);

    /**
     * Returns a suboperation for specified tree node
     *
     * @return Reference to suboperation for specified tree node, {@link Optional#absent()}
     *    if suboperation is not supported for specified tree node.
     */
    @Override
    public abstract Optional<ModificationApplyOperation> getChild(PathArgument child);

    abstract void recursivelyVerifyStructure(NormalizedNode<?, ?> value);
}
