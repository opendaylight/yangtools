/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * Node Modification Node and Tree
 *
 * Tree which structurally resembles data tree and captures client modifications
 * to the data store tree.
 *
 * This tree is lazily created and populated via {@link #modifyChild(PathArgument)}
 * and {@link TreeNode} which represents original state as tracked by {@link #getOriginal()}.
 */
@NotThreadSafe
final class ModifiedNode extends NodeModification implements StoreTreeNode<ModifiedNode> {
    static final Predicate<ModifiedNode> IS_TERMINAL_PREDICATE = new Predicate<ModifiedNode>() {
        @Override
        public boolean apply(@Nonnull final ModifiedNode input) {
            Preconditions.checkNotNull(input);
            switch (input.getOperation()) {
            case DELETE:
            case MERGE:
            case WRITE:
                return true;
            case TOUCH:
            case NONE:
                return false;
            }

            throw new IllegalArgumentException(String.format("Unhandled modification type %s", input.getOperation()));
        }
    };
    private static final int DEFAULT_CHILD_COUNT = 8;

    private final Map<PathArgument, ModifiedNode> children;
    private final Optional<TreeNode> original;
    private final PathArgument identifier;
    private LogicalOperation operation = LogicalOperation.NONE;
    private Optional<TreeNode> snapshotCache;
    private NormalizedNode<?, ?> value;
    private ModificationType modType;

    private ModifiedNode(final PathArgument identifier, final Optional<TreeNode> original, final ChildTrackingPolicy childPolicy) {
        this.identifier = identifier;
        this.original = original;

        switch (childPolicy) {
        case NONE:
            children = Collections.emptyMap();
            break;
        case ORDERED:
            children = new LinkedHashMap<>(DEFAULT_CHILD_COUNT);
            break;
        case UNORDERED:
            children = new HashMap<>(DEFAULT_CHILD_COUNT);
            break;
        default:
            throw new IllegalArgumentException("Unsupported child tracking policy " + childPolicy);
        }
    }

    /**
     * Return the value which was written to this node.
     *
     * @return Currently-written value
     */
    public NormalizedNode<?, ?> getWrittenValue() {
        return value;
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    Optional<TreeNode> getOriginal() {
        return original;
    }

    @Override
    LogicalOperation getOperation() {
        return operation;
    }

    /**
     *
     * Returns child modification if child was modified
     *
     * @return Child modification if direct child or it's subtree
     *  was modified.
     *
     */
    @Override
    public Optional<ModifiedNode> getChild(final PathArgument child) {
        return Optional.<ModifiedNode> fromNullable(children.get(child));
    }

    /**
     *
     * Returns child modification if child was modified, creates {@link ModifiedNode}
     * for child otherwise.
     *
     * If this node's {@link ModificationType} is {@link ModificationType#UNMODIFIED}
     * changes modification type to {@link ModificationType#SUBTREE_MODIFIED}
     *
     * @param child child identifier, may not be null
     * @param childPolicy child tracking policy for the node we are looking for
     * @return {@link ModifiedNode} for specified child, with {@link #getOriginal()}
     *         containing child metadata if child was present in original data.
     */
    ModifiedNode modifyChild(@Nonnull final PathArgument child, @Nonnull final ChildTrackingPolicy childPolicy) {
        clearSnapshot();
        if (operation == LogicalOperation.NONE) {
            updateOperationType(LogicalOperation.TOUCH);
        }
        final ModifiedNode potential = children.get(child);
        if (potential != null) {
            return potential;
        }

        final Optional<TreeNode> currentMetadata;
        if (original.isPresent()) {
            final TreeNode orig = original.get();
            currentMetadata = orig.getChild(child);
        } else {
            currentMetadata = Optional.absent();
        }

        final ModifiedNode newlyCreated = new ModifiedNode(child, currentMetadata, childPolicy);
        children.put(child, newlyCreated);
        return newlyCreated;
    }

    /**
     * Returns all recorded direct child modification
     *
     * @return all recorded direct child modifications
     */
    @Override
    Collection<ModifiedNode> getChildren() {
        return children.values();
    }

    /**
     * Records a delete for associated node.
     */
    void delete() {
        final LogicalOperation newType;

        switch (operation) {
        case DELETE:
        case NONE:
            // We need to record this delete.
            newType = LogicalOperation.DELETE;
            break;
        case MERGE:
        case TOUCH:
        case WRITE:
            /*
             * We are canceling a previous modification. This is a bit tricky,
             * as the original write may have just introduced the data, or it
             * may have modified it.
             *
             * As documented in BUG-2470, a delete of data introduced in this
             * transaction needs to be turned into a no-op.
             */
            newType = original.isPresent() ? LogicalOperation.DELETE : LogicalOperation.NONE;
            break;
        default:
            throw new IllegalStateException("Unhandled deletion of node with " + operation);
        }

        clearSnapshot();
        children.clear();
        this.value = null;
        updateOperationType(newType);
    }

    /**
     * Records a write for associated node.
     *
     * @param value
     */
    void write(final NormalizedNode<?, ?> value) {
        clearSnapshot();
        updateOperationType(LogicalOperation.WRITE);
        children.clear();
        this.value = value;
    }

    void merge(final NormalizedNode<?, ?> value) {
        clearSnapshot();
        updateOperationType(LogicalOperation.MERGE);

        /*
         * Blind overwrite of any previous data is okay, no matter whether the node
         * is simple or complex type.
         *
         * If this is a simple or complex type with unkeyed children, this merge will
         * be turned into a write operation, overwriting whatever was there before.
         *
         * If this is a container with keyed children, there are two possibilities:
         * - if it existed before, this value will never be consulted and the children
         *   will get explicitly merged onto the original data.
         * - if it did not exist before, this value will be used as a seed write and
         *   children will be merged into it.
         * In either case we rely on OperationWithModification to manipulate the children
         * before calling this method, so unlike a write we do not want to clear them.
         */
        this.value = value;
    }

    /**
     * Seal the modification node and prune any children which has not been
     * modified.
     */
    void seal(final ModificationApplyOperation schema) {
        clearSnapshot();

        // Walk all child nodes and remove any children which have not
        // been modified.
        final Iterator<ModifiedNode> it = children.values().iterator();
        while (it.hasNext()) {
            final ModifiedNode child = it.next();
            final Optional<ModificationApplyOperation> childSchema = schema.getChild(child.getIdentifier());
            Preconditions.checkState(childSchema.isPresent());
            child.seal(childSchema.get());

            if (child.operation == LogicalOperation.NONE) {
                it.remove();
            }
        }

        // A TOUCH node without any children is a no-op
        switch (operation) {
            case TOUCH:
                if (children.isEmpty()) {
                    updateOperationType(LogicalOperation.NONE);
                }
                break;
            case WRITE:
                schema.verifyStructure(value, true);
                break;
            default:
                break;
        }
    }

    private void clearSnapshot() {
        snapshotCache = null;
    }

    Optional<TreeNode> getSnapshot() {
        return snapshotCache;
    }

    Optional<TreeNode> setSnapshot(final Optional<TreeNode> snapshot) {
        snapshotCache = Preconditions.checkNotNull(snapshot);
        return snapshot;
    }

    private void updateOperationType(final LogicalOperation type) {
        operation = type;
        modType = null;
        clearSnapshot();
    }

    @Override
    public String toString() {
        return "NodeModification [identifier=" + identifier + ", modificationType="
                + operation + ", childModification=" + children + "]";
    }

    void resolveModificationType(@Nonnull final ModificationType type) {
        modType = type;
    }

    /**
     * Return the physical modification done to data. May return null if the
     * operation has not been applied to the underlying tree. This is different
     * from the logical operation in that it can actually be a no-op if the
     * operation has no side-effects (like an empty merge on a container).
     *
     * @return Modification type.
     */
    ModificationType getModificationType() {
        return modType;
    }

    /**
     * Create a node which will reflect the state of this node, except it will behave as newly-written
     * value. This is useful only for merge validation.
     *
     * @param value Value associated with the node
     * @return An isolated node. This node should never reach a datatree.
     */
    ModifiedNode asNewlyWritten(final NormalizedNode<?, ?> value) {
        /*
         * We are instantiating an "equivalent" of this node. Currently the only callsite does not care
         * about the actual iteration order, so we do not have to specify the same tracking policy as
         * we were instantiated with. Since this is the only time we need to know that policy (it affects
         * only things in constructor), we do not want to retain it (saves some memory on per-instance
         * basis).
         *
         * We could reconstruct it using two instanceof checks (to undo what the constructor has done),
         * which would give perfect results. The memory saving would be at most 32 bytes of a short-lived
         * object, so let's not bother with that.
         */
        final ModifiedNode ret = new ModifiedNode(getIdentifier(), Optional.<TreeNode>absent(), ChildTrackingPolicy.UNORDERED);
        ret.write(value);
        return ret;
    }

    public static ModifiedNode createUnmodified(final TreeNode metadataTree, final ChildTrackingPolicy childPolicy) {
        return new ModifiedNode(metadataTree.getIdentifier(), Optional.of(metadataTree), childPolicy);
    }
}
