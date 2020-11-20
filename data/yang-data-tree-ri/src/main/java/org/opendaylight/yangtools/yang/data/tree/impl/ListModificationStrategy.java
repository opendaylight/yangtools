/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class ListModificationStrategy extends SchemaAwareApplyOperation<ListSchemaNode> {
    private static final NormalizedNodeContainerSupport<NodeIdentifier, UnkeyedListEntryNode> ITEM_SUPPORT =
            new NormalizedNodeContainerSupport<>(UnkeyedListEntryNode.class,
                    ImmutableUnkeyedListEntryNodeBuilder::create, ImmutableUnkeyedListEntryNodeBuilder::create);

    private final DataNodeContainerModificationStrategy<ListSchemaNode> entryStrategy;
    private final UnkeyedListNode emptyNode;

    ListModificationStrategy(final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig, final UniqueTreeNodeSupport<?> unique) {
        super(unique == null ? TreeNodeSupport.DEFAULT : new UniqueIndexTreeNodeSupport(unique));
        entryStrategy = new DataNodeContainerModificationStrategy<>(unique == null ? TreeNodeSupport.DEFAULT
            : new UniqueVectorTreeNodeSupport(unique), ITEM_SUPPORT, schema, treeConfig);
        emptyNode = ImmutableNodes.listNode(schema.getQName());
    }

    static @NonNull ListModificationStrategy of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final ImmutableList<UniqueValidator<?>> validators = UniqueValidation.validatorsOf(schema, treeConfig);
        return new ListModificationStrategy(schema, treeConfig,
            validators.isEmpty() ? null : UniqueTreeNodeSupport.of(validators));
    }

    @Override ListSchemaNode getSchema() {
        return entryStrategy.getSchema();
    }

    @Override Optional<? extends TreeNode> apply(final ModifiedNode modification,
            final Optional<? extends TreeNode> storeMeta, final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.ORDERED;
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        throw new IllegalStateException(String.format("Merge of modification %s on unkeyed list should never be called",
            modification));
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        throw new UnsupportedOperationException("UnkeyedList does not support subtree change.");
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final Optional<? extends TreeNode> currentMeta, final Version version) {
        final UnkeyedListNode newNode = (UnkeyedListNode) newValue;
        if (modification.getChildren().isEmpty()) {
            return newTreeNode(newNode, version);
        }

        /*
         * This is where things get interesting. The user has performed a write and then she applied some more
         * modifications to it. So we need to make sense of that an apply the operations on top of the written value. We
         * could have done it during the write, but this operation is potentially expensive, so we have left it out of
         * the fast path.
         *
         * As it turns out, once we materialize the written data, we can share the code path with the subtree change. So
         * let's create an unsealed TreeNode and run the common parts on it -- which end with the node being sealed.
         */
        final MutableTreeNode mutable = newMutableTreeNode(newNode, version);
        mutable.setSubtreeVersion(version);

        return mutateChildren(mutable, ImmutableUnkeyedListNodeBuilder.create(newNode), version,
            modification.getChildren());
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
            final Optional<? extends TreeNode> cm = meta.findChildByArg(id);

            final Optional<? extends TreeNode> result = resolveChildOperation(id).apply(mod, cm, nodeVersion);
            if (result.isPresent()) {
                final TreeNode tn = result.get();
                meta.putChild(tn);
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
    public ModificationApplyOperation childByArg(final PathArgument arg) {
        return arg instanceof NodeIdentifier ? entryStrategy : null;
    }

    @Override
    void verifyValue(final NormalizedNode value) {
        // NOOP
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode value) {
        // NOOP
    }

    @Override
    protected void checkTouchApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path.toInstanceIdentifier(), "Subtree modification is not allowed.");
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode value, final Version version) {
        // Unkeyed lists are always replaced
        node.write(value);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("entry", entryStrategy);
    }
}
