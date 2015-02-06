/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnkeyedListModificationStrategy extends SchemaAwareApplyOperation {

    private final Optional<ModificationApplyOperation> entryStrategy;
    private final ListSchemaNode schema;

    protected UnkeyedListModificationStrategy(final ListSchemaNode schema) {
        entryStrategy = Optional.<ModificationApplyOperation> of(new DataNodeContainerModificationStrategy.UnkeyedListItemModificationStrategy(schema));
        this.schema = schema;
    }

    @Override
    boolean isOrdered() {
        return true;
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        return applyWrite(modification, Optional.of(currentMeta), version);
    }

    @Override
    protected TreeNode applySubtreeChange(final ModifiedNode modification,
            final TreeNode currentMeta, final Version version) {
        throw new UnsupportedOperationException("UnkeyedList does not support subtree change.");
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification,
            final Optional<TreeNode> currentMeta, final Version version) {
        final NormalizedNode<?, ?> newValue = modification.getWrittenValue();
        final TreeNode newValueMeta = TreeNodeFactory.createTreeNode(newValue, version);

        if (Iterables.isEmpty(modification.getChildren())) {
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
         */
        final MutableTreeNode mutable = newValueMeta.mutable();
        mutable.setSubtreeVersion(version);

        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder dataBuilder = ImmutableUnkeyedListEntryNodeBuilder
            .create((UnkeyedListEntryNode) newValue);

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
            final PathArgument id = mod.getIdentifier();
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
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        if (child instanceof NodeIdentifier) {
            return entryStrategy;
        }
        return Optional.absent();
    }

    @Override
    protected void verifyWrittenStructure(final NormalizedNode<?, ?> writtenValue) {

    }

    @Override
    protected void checkSubtreeModificationApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path, "Subtree modification is not allowed.");
    }

    @Override
    protected void checkMergeApplicable(YangInstanceIdentifier path, NodeModification modification, Optional<TreeNode> current) throws DataValidationFailedException {
        super.checkMergeApplicable(path, modification, current);
        checkMinMaxElements(path, schema, (ModifiedNode) modification, current);

    }

    @Override
    protected void checkWriteApplicable(YangInstanceIdentifier path, NodeModification modification, Optional<TreeNode> current) throws DataValidationFailedException {
        super.checkWriteApplicable(path, modification, current);
        checkMinMaxElements(path, schema, (ModifiedNode) modification, current);
    }

    private void checkMinMaxElements(YangInstanceIdentifier path, ListSchemaNode schema, ModifiedNode modification,
                                     Optional<TreeNode> current) throws DataValidationFailedException {
        int childrenAfter = ((UnkeyedListNode) modification.getWrittenValue()).getSize();
        for (ModifiedNode modChild : modification.getChildren()) {
            if (modChild.getType().equals(ModificationType.WRITE) ||
                    (modChild.getType().equals(ModificationType.MERGE) && !current.isPresent())) {
                childrenAfter++;
            } else if (modChild.getType().equals(ModificationType.DELETE)) {
                childrenAfter--;
            }
        }

        int childrenBefore = current.isPresent() ?
                ((UnkeyedListNode) current.get().getData()).getSize() : 0;
        final int childrenTotal = childrenBefore + childrenAfter;

        // TODO: remove this null checks when Bug 2685 is fixed
        final int minElements = schema.getConstraints().getMinElements() != null ?
                schema.getConstraints().getMinElements() : 0;
        final int maxElements = schema.getConstraints().getMaxElements() != null ?
                schema.getConstraints().getMaxElements() : Integer.MAX_VALUE;

        if (minElements > childrenTotal || maxElements < childrenTotal) {
            throw new DataValidationFailedException(path, "Number of elements '" + (childrenAfter + childrenBefore)
                    + "' of '" + schema.getQName().getLocalName() + "' is not in allowed range <" + minElements +
                    ", " + maxElements + ">.");
        }
    }
}
