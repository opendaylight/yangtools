/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModifiedNodeDoesNotExistException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;

abstract class AbstractNodeContainerModificationStrategy extends SchemaAwareApplyOperation {

    private final Class<? extends NormalizedNode<?, ?>> nodeClass;

    protected AbstractNodeContainerModificationStrategy(final Class<? extends NormalizedNode<?, ?>> nodeClass) {
        this.nodeClass = Preconditions.checkNotNull(nodeClass);
    }

    @Override
    void verifyStructure(final ModifiedNode modification) throws IllegalArgumentException {
        for (ModifiedNode childModification : modification.getChildren()) {
            resolveChildOperation(childModification.getIdentifier()).verifyStructure(childModification);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void verifyWrittenStructure(final NormalizedNode<?, ?> writtenValue) {
        checkArgument(nodeClass.isInstance(writtenValue), "Node %s is not of type %s", writtenValue, nodeClass);
        checkArgument(writtenValue instanceof NormalizedNodeContainer);

        NormalizedNodeContainer container = (NormalizedNodeContainer) writtenValue;
        for (Object child : container.getValue()) {
            checkArgument(child instanceof NormalizedNode);

            /*
             * FIXME: fail-fast semantics:
             *
             * We can validate the data structure here, aborting the commit
             * before it ever progresses to being committed.
             */
        }
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification,
            final Optional<TreeNode> currentMeta, final Version version) {
        final NormalizedNode<?, ?> newValue = modification.getWrittenValue();
        final TreeNode newValueMeta = TreeNodeFactory.createTreeNode(newValue, version);

        if (modification.getChildren().isEmpty()) {
            return newValueMeta;
        }

        /*
         * This is where things get interesting. The user has performed a write and
         * then she applied some more modifications to it. So we need to make sense
         * of that an apply the operations on top of the written value. We could have
         * done it during the write, but this operation is potentially expensive, so
         * we have left it out of the fast path.
         *
         * As it turns out, once we materialize the written data, we can share the
         * code path with the subtree change. So let's create an unsealed TreeNode
         * and run the common parts on it -- which end with the node being sealed.
         *
         * FIXME: this code needs to be moved out from the prepare() path and into
         *        the read() and seal() paths. Merging of writes needs to be charged
         *        to the code which originated this, not to the code which is
         *        attempting to make it visible.
         */
        final MutableTreeNode mutable = newValueMeta.mutable();
        mutable.setSubtreeVersion(version);

        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder dataBuilder = createBuilder(newValue);

        return mutateChildren(mutable, dataBuilder, version, modification.getChildren());
    }

    /**
     * Applies write/remove diff operation for each modification child in modification subtree.
     * Operation also sets the Data tree references for each Tree Node (Index Node) in meta (MutableTreeNode) structure.
     *
     * @param meta MutableTreeNode (IndexTreeNode)
     * @param data DataBuilder
     * @param nodeVersion Version of TreeNode
     * @param modifications modification operations to apply
     * @return Sealed immutable copy of TreeNode structure with all Data Node references set.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TreeNode mutateChildren(final MutableTreeNode meta, final NormalizedNodeContainerBuilder data,
            final Version nodeVersion, final Iterable<ModifiedNode> modifications) {

        for (ModifiedNode mod : modifications) {
            final YangInstanceIdentifier.PathArgument id = mod.getIdentifier();
            final Optional<TreeNode> cm = meta.getChild(id);

            Optional<TreeNode> result = resolveChildOperation(id).apply(mod, cm, nodeVersion);
            if (result.isPresent()) {
                final TreeNode tn = result.get();
                meta.addChild(tn);
                data.addChild(tn.getData());
            } else {
                meta.removeChild(id);
                data.removeChild(id);
            }
        }

        meta.setData(data.build());
        return meta.seal();
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        // For Node Containers - merge is same as subtree change - we only replace children.
        return applySubtreeChange(modification, currentMeta, version);
    }

    @Override
    public TreeNode applySubtreeChange(final ModifiedNode modification,
            final TreeNode currentMeta, final Version version) {
        final MutableTreeNode newMeta = currentMeta.mutable();
        newMeta.setSubtreeVersion(version);

        /*
         * The user has issued an empty merge operation. In this case we do not perform
         * a data tree mutation, do not pass GO, and do not collect useless garbage.
         */
        final Collection<ModifiedNode> children = modification.getChildren();
        if (children.isEmpty()) {
            modification.resolveModificationType(ModificationType.UNMODIFIED);
            newMeta.setData(currentMeta.getData());
            return newMeta.seal();
        }

        @SuppressWarnings("rawtypes")
        NormalizedNodeContainerBuilder dataBuilder = createBuilder(currentMeta.getData());

        /*
         * TODO: this is not entirely accurate. If there is only an empty merge operation
         *       among the children, its effect is ModificationType.UNMODIFIED. That would
         *       mean this operation can be turned into UNMODIFIED, cascading that further
         *       up the root -- potentially turning the entire transaction into a no-op
         *       from the perspective of physical replication.
         *
         *       In order to do that, though, we either have to walk the children ourselves
         *       (looking for a non-UNMODIFIED child), or have mutateChildren() pass that
         *       information back to us.
         */
        modification.resolveModificationType(ModificationType.SUBTREE_MODIFIED);
        return mutateChildren(newMeta, dataBuilder, version, children);
    }

    @Override
    protected void checkSubtreeModificationApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        if (!modification.getOriginal().isPresent() && !current.isPresent()) {
            throw new ModifiedNodeDoesNotExistException(path, String.format("Node %s does not exist. Cannot apply modification to its children.", path));
        }

        SchemaAwareApplyOperation.checkConflicting(path, current.isPresent(), "Node was deleted by other transaction.");
        checkChildPreconditions(path, modification, current);
    }

    private void checkChildPreconditions(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        final TreeNode currentMeta = current.get();
        for (NodeModification childMod : modification.getChildren()) {
            final YangInstanceIdentifier.PathArgument childId = childMod.getIdentifier();
            final Optional<TreeNode> childMeta = currentMeta.getChild(childId);

            YangInstanceIdentifier childPath = path.node(childId);
            resolveChildOperation(childId).checkApplicable(childPath, childMod, childMeta);
        }
    }

    @Override
    protected void checkMergeApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException {
        if(current.isPresent()) {
            checkChildPreconditions(path, modification,current);
        }
    }

    @SuppressWarnings("rawtypes")
    protected abstract NormalizedNodeContainerBuilder createBuilder(NormalizedNode<?, ?> original);
}
