/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class AbstractMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final UniqueIndexStrategyBase uniqueIndexUpdateStrategy;

    protected AbstractMapModificationStrategy(final ListSchemaNode schema, final Class<? extends NormalizedNode<?, ?>> nodeClass,
            final DataTreeConfiguration treeConfig) {
        super(nodeClass, treeConfig);
        uniqueIndexUpdateStrategy = UniqueIndexStrategyBase.forList(schema, treeConfig);
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        /*
         * The user may have issued an empty merge operation. In this case we do not perform
         * a data tree mutation, do not pass GO, and do not collect useless garbage. It
         * also means the ModificationType is UNMODIFIED.
         */
        final Collection<ModifiedNode> children = modification.getChildren();
        if (!children.isEmpty()) {
            final TreeNode ret = mutateChildren(currentMeta, version, children);

            /*
             * It is possible that the only modifications under this node were empty merges,
             * which were turned into UNMODIFIED. If that is the case, we can turn this operation
             * into UNMODIFIED, too, potentially cascading it up to root. This has the benefit
             * of speeding up any users, who can skip processing child nodes.
             *
             * In order to do that, though, we have to check all child operations are UNMODIFIED.
             * Let's do precisely that, stopping as soon we find a different result.
             */
            for (final ModifiedNode child : children) {
                if (child.getModificationType() != ModificationType.UNMODIFIED) {
                    modification.resolveModificationType(ModificationType.SUBTREE_MODIFIED);
                    return ret;
                }
            }
        }

        // The merge operation did not have any children, or all of them turned out to be UNMODIFIED, hence do not
        // replace the metadata node.
        modification.resolveModificationType(ModificationType.UNMODIFIED);
        return currentMeta;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification,
            final Optional<TreeNode> currentMeta, final Version version) {
        final NormalizedNode<?, ?> newValue = modification.getWrittenValue();
        final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> uniqueIndexes = uniqueIndexUpdateStrategy.createIndexesFromData(newValue);
        final TreeNode newValueMeta = TreeNodeFactory.createTreeNode(newValue, version, uniqueIndexes);

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
        final TreeNode result = mutateChildren(newValueMeta, version, modification.getChildren());

        // We are good to go except one detail: this is a single logical write, but
        // we have a result TreeNode which has been forced to materialized, e.g. it
        // is larger than it needs to be. Create a new TreeNode to host the data.
        return TreeNodeFactory.createTreeNode(result.getData(), version, result.getIndexes());
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
    private TreeNode mutateChildren(final TreeNode currentMeta,
            final Version nodeVersion, final Iterable<ModifiedNode> modifications) {

        final NormalizedNodeContainerBuilder data = createBuilder(currentMeta.getData());
        final MutableTreeNode mutable = currentMeta.mutable();
        mutable.setSubtreeVersion(nodeVersion);

        final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes = currentMeta.getIndexes();
        if(treeNodeIndexes.isEmpty() && !uniqueIndexUpdateStrategy.getUniqueConstraintsLeafIds().isEmpty()){
            mutable.buildIndexes(uniqueIndexUpdateStrategy);
        }
        for (final ModifiedNode mod : modifications) {
            final YangInstanceIdentifier.PathArgument id = mod.getIdentifier();
            final Optional<TreeNode> cm = mutable.getChild(id);

            final Optional<TreeNode> result = resolveChildOperation(id).apply(mod, cm, nodeVersion);
            if (result.isPresent()) {
                final TreeNode tn = result.get();
                mutable.addChild(tn, uniqueIndexUpdateStrategy);
                data.addChild(tn.getData());
            } else {
                mutable.removeChild(id, uniqueIndexUpdateStrategy);
                data.removeChild(id);
            }
        }

        mutable.setData(data.build());
        return mutable.seal();
    }
}
