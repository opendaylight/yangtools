/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

/**
 * An operation responsible for applying {@link ModifiedNode} on tree. The operation is a hierachical composite -
 * the operation on top level node consists of suboperations on child nodes. This allows to walk operation hierarchy and
 * invoke suboperations independently.
 *
 * <p>
 * <b>Implementation notes</b>
 * <ul>
 *   <li>Implementations MUST expose all nested suboperations which operates on child nodes expose via
 *       {@link #findChildByArg(PathArgument)} method.</li>
 *   <li>Same suboperations SHOULD be used when invoked via {@link #apply(ModifiedNode, Optional, Version)},
 *       if applicable.</li>
 *   <li>There are exactly two base implementations:
 *     <ul>
 *       <li>{@link SchemaAwareApplyOperation}, which serves as the base class for stateful mutators -- directly
 *           impacting the layout and transitions of the {@link TreeNode} hierarchy.
 *       <li>{@link AbstractValidation}, which serves as the base class for stateless checks, which work purely on top
 *           of the {@link TreeNode} hierarchy. These are always overlaid on top of some other
 *           {@link ModificationApplyOperation}, ultimately leading to a {@link SchemaAwareApplyOperation}.
 *     </ul>
 *     This allows baseline invocations from {@link OperationWithModification} to be bimorphic in the first line of
 *     dispatch.
 *   </li>
 * </ul>
 */
abstract class ModificationApplyOperation implements StoreTreeNode<ModificationApplyOperation> {
    /**
     * Implementation of this operation must be stateless and must not change state of this object.
     *
     * @param modification
     *            NodeModification to be applied
     * @param storeMeta
     *            Store Metadata Node on which NodeModification should be
     *            applied
     * @param version New subtree version of parent node
     * @return new {@link TreeNode} if operation resulted in updating
     *         node, {@link Optional#absent()} if {@link ModifiedNode}
     *         resulted in deletion of this node.
     * @throws IllegalArgumentException
     *             If it is not possible to apply Operation on provided Metadata
     *             node
     */
    abstract Optional<? extends TreeNode> apply(ModifiedNode modification, Optional<? extends TreeNode> storeMeta,
            Version version);

    /**
     * Checks if provided node modification could be applied to current metadata node.
     *
     * @param path Path to modification
     * @param modification Modification
     * @param current Metadata Node to which modification should be applied
     * @param version Metadata version
     * @throws DataValidationFailedException if the modification is not applicable
     */
    abstract void checkApplicable(ModificationPath path, NodeModification modification,
            Optional<? extends TreeNode> current, Version version) throws DataValidationFailedException;

    /**
     * Performs a quick structural verification of NodeModification, such as written values / types uses right
     * structural elements.
     *
     * @param modification data to be verified.
     * @throws IllegalArgumentException If provided NodeModification does not adhere to the
     *         structure.
     */
    abstract void quickVerifyStructure(NormalizedNode modification);

    /**
     * Performs a full structural verification of NodeModification, such as written values / types uses right
     * structural elements. Unlike {@link #quickVerifyStructure(NormalizedNode)} this includes recursively checking
     * children, too.
     *
     * @param modification data to be verified.
     * @throws IllegalArgumentException If provided NodeModification does not adhere to the
     *         structure.
     */
    abstract void fullVerifyStructure(NormalizedNode modification);

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
    abstract void mergeIntoModifiedNode(ModifiedNode modification, NormalizedNode value, Version version);

    /**
     * {@inheritDoc}
     *
     * @return Reference to suboperation for specified tree node, {@code null} if suboperation is not supported for
     *         specified tree node.
     */
    @Override
    public abstract ModificationApplyOperation childByArg(PathArgument arg);

    abstract void recursivelyVerifyStructure(NormalizedNode value);

    abstract TreeNode newTreeNode(NormalizedNode newValue, Version version);

    abstract MutableTreeNode newMutableTreeNode(NormalizedNode newValue, Version version);

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }
}
