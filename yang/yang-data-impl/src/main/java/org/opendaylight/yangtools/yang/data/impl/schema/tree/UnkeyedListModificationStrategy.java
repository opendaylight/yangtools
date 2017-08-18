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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnkeyedListModificationStrategy extends SchemaAwareApplyOperation {

    private final Optional<ModificationApplyOperation> entryStrategy;

    UnkeyedListModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        entryStrategy = Optional.of(new UnkeyedListItemModificationStrategy(schema, treeConfig));
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.ORDERED;
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        throw new IllegalStateException(String.format("Merge of modification %s on unkeyed list should never be called",
            modification));
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification,
            final TreeNode currentMeta, final Version version) {
        throw new UnsupportedOperationException("UnkeyedList does not support subtree change.");
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

        for (final ModifiedNode mod : modifications) {
            final PathArgument id = mod.getIdentifier();
            final Optional<TreeNode> cm = meta.getChild(id);

            final Optional<TreeNode> result = resolveChildOperation(id).apply(mod, cm, nodeVersion);
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
        return child instanceof NodeIdentifier ? entryStrategy : Optional.empty();
    }

    @Override
    protected void verifyStructure(final NormalizedNode<?, ?> writtenValue, final boolean verifyChildren) {

    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        // NOOP
    }

    @Override
    protected void checkTouchApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path, "Subtree modification is not allowed.");
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        // Unkeyed lists are always replaced
        node.write(value);
    }
}
