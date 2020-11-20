/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Verify;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.api.ModifiedNodeDoesNotExistException;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

abstract class AbstractNodeContainerModificationStrategy<T extends WithStatus>
        extends SchemaAwareApplyOperation<T> {
    abstract static class Invisible<T extends WithStatus> extends AbstractNodeContainerModificationStrategy<T> {
        private final @NonNull SchemaAwareApplyOperation<T> entryStrategy;

        Invisible(final NormalizedNodeContainerSupport<?, ?> support,
                final DataTreeConfiguration treeConfig, final SchemaAwareApplyOperation<T> entryStrategy) {
            this(TreeNodeSupport.DEFAULT, support, treeConfig, entryStrategy);
        }

        Invisible(final TreeNodeSupport treeSupport, final NormalizedNodeContainerSupport<?, ?> support,
                final DataTreeConfiguration treeConfig, final SchemaAwareApplyOperation<T> entryStrategy) {
            super(treeSupport, support, treeConfig);
            this.entryStrategy = requireNonNull(entryStrategy);
        }

        @Override
        final T getSchema() {
            return entryStrategy.getSchema();
        }

        final @NonNull ModificationApplyOperation entryStrategy() {
            return entryStrategy;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("entry", entryStrategy);
        }
    }

    abstract static class Visible<T extends WithStatus> extends AbstractNodeContainerModificationStrategy<T> {
        private final @NonNull T schema;

        Visible(final TreeNodeSupport treeSupport, final NormalizedNodeContainerSupport<?, ?> support,
                final DataTreeConfiguration treeConfig, final T schema) {
            super(treeSupport, support, treeConfig);
            this.schema = requireNonNull(schema);
        }

        Visible(final NormalizedNodeContainerSupport<?, ?> support, final DataTreeConfiguration treeConfig,
                final T schema) {
            this(TreeNodeSupport.DEFAULT, support, treeConfig, schema);
        }

        @Override
        final T getSchema() {
            return schema;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("schema", schema);
        }
    }

    /**
     * Fake TreeNode version used in
     * {@link #checkTouchApplicable(ModificationPath, NodeModification, Optional, Version)}
     * It is okay to use a global constant, as the delegate will ignore it anyway.
     */
    private static final Version FAKE_VERSION = Version.initial();

    private final NormalizedNodeContainerSupport<?, ?> support;
    private final boolean verifyChildrenStructure;

    AbstractNodeContainerModificationStrategy(final TreeNodeSupport treeSupport,
            final NormalizedNodeContainerSupport<?, ?> support, final DataTreeConfiguration treeConfig) {
        super(treeSupport);
        this.support = requireNonNull(support);
        verifyChildrenStructure = treeConfig.getTreeType() == TreeType.CONFIGURATION;
    }

    @Override
    protected final ChildTrackingPolicy getChildPolicy() {
        return support.childPolicy;
    }

    @Override
    final void verifyValue(final NormalizedNode writtenValue) {
        final Class<?> nodeClass = support.requiredClass;
        checkArgument(nodeClass.isInstance(writtenValue), "Node %s is not of type %s", writtenValue, nodeClass);
        checkArgument(writtenValue instanceof NormalizedNodeContainer);
    }

    @Override
    final void verifyValueChildren(final NormalizedNode writtenValue) {
        if (verifyChildrenStructure) {
            final DistinctNodeContainer<?, ?> container = (DistinctNodeContainer<?, ?>) writtenValue;
            for (final NormalizedNode child : container.body()) {
                final ModificationApplyOperation childOp = childByArg(child.getIdentifier());
                if (childOp == null) {
                    throw new SchemaValidationFailedException(String.format(
                        "Node %s is not a valid child of %s according to the schema.",
                        child.getIdentifier(), container.getIdentifier()));
                }
                childOp.fullVerifyStructure(child);
            }

            optionalVerifyValueChildren(writtenValue);
        }
        mandatoryVerifyValueChildren(writtenValue);
    }

    /**
     * Perform additional verification on written value's child structure, like presence of mandatory children and
     * exclusion. The default implementation does nothing and is not invoked for non-CONFIG data trees.
     *
     * @param writtenValue Effective written value
     */
    void optionalVerifyValueChildren(final NormalizedNode writtenValue) {
        // Defaults to no-op
    }

    /**
     * Perform additional verification on written value's child structure, like presence of mandatory children.
     * The default implementation does nothing.
     *
     * @param writtenValue Effective written value
     */
    void mandatoryVerifyValueChildren(final NormalizedNode writtenValue) {
        // Defaults to no-op
    }

    @Override
    protected final void recursivelyVerifyStructure(final NormalizedNode value) {
        final NormalizedNodeContainer<?> container = (NormalizedNodeContainer<?>) value;
        for (final NormalizedNode child : container.body()) {
            final ModificationApplyOperation childOp = childByArg(child.getIdentifier());
            if (childOp == null) {
                throw new SchemaValidationFailedException(
                    String.format("Node %s is not a valid child of %s according to the schema.",
                        child.getIdentifier(), container.getIdentifier()));
            }

            childOp.recursivelyVerifyStructure(child);
        }
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final Optional<? extends TreeNode> currentMeta, final Version version) {
        if (modification.getChildren().isEmpty()) {
            return newTreeNode(newValue, version);
        }

        /*
         * This is where things get interesting. The user has performed a write and then she applied some more
         * modifications to it. So we need to make sense of that and apply the operations on top of the written value.
         *
         * We could have done it during the write, but this operation is potentially expensive, so we have left it out
         * of the fast path.
         *
         * As it turns out, once we materialize the written data, we can share the code path with the subtree change. So
         * let's create an unsealed TreeNode and run the common parts on it -- which end with the node being sealed.
         *
         * FIXME: this code needs to be moved out from the prepare() path and into the read() and seal() paths. Merging
         *        of writes needs to be charged to the code which originated this, not to the code which is attempting
         *        to make it visible.
         */
        final MutableTreeNode mutable = newMutableTreeNode(newValue, version);
        mutable.setSubtreeVersion(version);

        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder dataBuilder = support.createBuilder(newValue);
        final TreeNode result = mutateChildren(mutable, dataBuilder, version, modification.getChildren());

        // We are good to go except one detail: this is a single logical write, but
        // we have a result TreeNode which has been forced to materialized, e.g. it
        // is larger than it needs to be. Create a new TreeNode to host the data.
        return newTreeNode(result.getData(), version);
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
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        /*
         * The node which we are merging exists. We now need to expand any child operations implied by the value. Once
         * we do that, ModifiedNode children will look like this node were a TOUCH and we will let applyTouch() do the
         * heavy lifting of applying the children recursively (either through here or through applyWrite().
         */
        final NormalizedNode value = modification.getWrittenValue();

        Verify.verify(value instanceof DistinctNodeContainer, "Attempted to merge non-container %s", value);
        for (final NormalizedNode c : ((DistinctNodeContainer<?, ?>) value).body()) {
            final PathArgument id = c.getIdentifier();
            modification.modifyChild(id, resolveChildOperation(id), version);
        }
        return applyTouch(modification, currentMeta, version);
    }

    private void mergeChildrenIntoModification(final ModifiedNode modification,
            final Collection<? extends NormalizedNode> children, final Version version) {
        for (final NormalizedNode c : children) {
            final ModificationApplyOperation childOp = resolveChildOperation(c.getIdentifier());
            final ModifiedNode childNode = modification.modifyChild(c.getIdentifier(), childOp, version);
            childOp.mergeIntoModifiedNode(childNode, c, version);
        }
    }

    @Override
    final void mergeIntoModifiedNode(final ModifiedNode modification, final NormalizedNode value,
            final Version version) {
        final Collection<? extends NormalizedNode> children = ((DistinctNodeContainer<?, ?>)value).body();
        switch (modification.getOperation()) {
            case NONE:
                // Fresh node, just record a MERGE with a value
                recursivelyVerifyStructure(value);
                modification.updateValue(LogicalOperation.MERGE, value);
                return;
            case TOUCH:

                mergeChildrenIntoModification(modification, children, version);
                // We record empty merge value, since real children merges are already expanded. This is needed to
                // satisfy non-null for merge original merge value can not be used since it mean different order of
                // operation - parent changes are always resolved before children ones, and having node in TOUCH means
                // children was modified before.
                modification.updateValue(LogicalOperation.MERGE, support.createEmptyValue(value));
                return;
            case MERGE:
                // Merging into an existing node. Merge data children modifications (maybe recursively) and mark
                // as MERGE, invalidating cached snapshot
                mergeChildrenIntoModification(modification, children, version);
                modification.updateOperationType(LogicalOperation.MERGE);
                return;
            case DELETE:
                // Delete performs a data dependency check on existence of the node. Performing a merge on DELETE means
                // we are really performing a write. One thing that ruins that are any child modifications. If there
                // are any, we will perform a read() to get the current state of affairs, turn this into into a WRITE
                // and then append any child entries.
                if (!modification.getChildren().isEmpty()) {
                    // Version does not matter here as we'll throw it out
                    final Optional<? extends TreeNode> current = apply(modification, modification.getOriginal(),
                        Version.initial());
                    if (current.isPresent()) {
                        modification.updateValue(LogicalOperation.WRITE, current.get().getData());
                        mergeChildrenIntoModification(modification, children, version);
                        return;
                    }
                }

                modification.updateValue(LogicalOperation.WRITE, value);
                return;
            case WRITE:
                // We are augmenting a previous write. We'll just walk value's children, get the corresponding
                // ModifiedNode and run recursively on it
                mergeChildrenIntoModification(modification, children, version);
                modification.updateOperationType(LogicalOperation.WRITE);
                return;
            default:
                throw new IllegalArgumentException("Unsupported operation " + modification.getOperation());
        }
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        /*
         * The user may have issued an empty merge operation. In this case we:
         * - do not perform a data tree mutation
         * - do not pass GO, and
         * - do not collect useless garbage.
         * It also means the ModificationType is UNMODIFIED.
         */
        final Collection<ModifiedNode> children = modification.getChildren();
        if (!children.isEmpty()) {
            @SuppressWarnings("rawtypes")
            final NormalizedNodeContainerBuilder dataBuilder = support.createBuilder(currentMeta.getData());
            final MutableTreeNode newMeta = currentMeta.mutable();
            newMeta.setSubtreeVersion(version);
            final TreeNode ret = mutateChildren(newMeta, dataBuilder, version, children);

            /*
             * It is possible that the only modifications under this node were empty merges, which were turned into
             * UNMODIFIED. If that is the case, we can turn this operation into UNMODIFIED, too, potentially cascading
             * it up to root. This has the benefit of speeding up any users, who can skip processing child nodes.
             *
             * In order to do that, though, we have to check all child operations are UNMODIFIED.
             *
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
    protected final void checkTouchApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        final TreeNode currentNode;
        if (!current.isPresent()) {
            currentNode = defaultTreeNode();
            if (currentNode == null) {
                if (!modification.getOriginal().isPresent()) {
                    final YangInstanceIdentifier id = path.toInstanceIdentifier();
                    throw new ModifiedNodeDoesNotExistException(id,
                        String.format("Node %s does not exist. Cannot apply modification to its children.", id));
                }

                throw new ConflictingModificationAppliedException(path.toInstanceIdentifier(),
                    "Node was deleted by other transaction.");
            }
        } else {
            currentNode = current.get();
        }

        checkChildPreconditions(path, modification, currentNode, version);
    }

    /**
     * Return the default tree node. Default implementation does nothing, but can be overridden to call
     * {@link #defaultTreeNode(NormalizedNode)}.
     *
     * @return Default empty tree node, or null if no default is available
     */
    @Nullable TreeNode defaultTreeNode() {
        // Defaults to no recovery
        return null;
    }

    static final TreeNode defaultTreeNode(final NormalizedNode emptyNode) {
        return TreeNode.of(emptyNode, FAKE_VERSION);
    }

    @Override
    protected final void checkMergeApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        if (current.isPresent()) {
            checkChildPreconditions(path, modification, current.get(), version);
        }
    }

    /**
     * Recursively check child preconditions.
     *
     * @param path current node path
     * @param modification current modification
     * @param current Current data tree node.
     */
    private void checkChildPreconditions(final ModificationPath path, final NodeModification modification,
            final TreeNode current, final Version version) throws DataValidationFailedException {
        for (final NodeModification childMod : modification.getChildren()) {
            final PathArgument childId = childMod.getIdentifier();
            final Optional<? extends TreeNode> childMeta = current.findChildByArg(childId);

            path.push(childId);
            try {
                resolveChildOperation(childId).checkApplicable(path, childMod, childMeta, version);
            } finally {
                path.pop();
            }
        }
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("support", support).add("verifyChildren", verifyChildrenStructure);
    }
}
