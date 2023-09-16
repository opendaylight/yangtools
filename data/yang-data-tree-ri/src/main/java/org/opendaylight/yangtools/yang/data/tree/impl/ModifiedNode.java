/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

/**
 * Node Modification Node and Tree.
 *
 * <p>
 * Tree which structurally resembles data tree and captures client modifications to the data store tree. This tree is
 * lazily created and populated via {@link #modifyChild(PathArgument, ModificationApplyOperation, Version)} and
 * {@link TreeNode} which represents original state as tracked by {@link #getOriginal()}.
 *
 * <p>
 * The contract is that the state information exposed here preserves the temporal ordering of whatever modifications
 * were executed. A child's effects pertain to data node as modified by its ancestors. This means that in order to
 * reconstruct the effective data node presentation, it is sufficient to perform a depth-first pre-order traversal of
 * the tree.
 */
final class ModifiedNode extends NodeModification implements StoreTreeNode<ModifiedNode> {
    private final Map<PathArgument, ModifiedNode> children;
    private final @Nullable TreeNode original;
    private final @NonNull PathArgument identifier;

    private LogicalOperation operation = LogicalOperation.NONE;
    private Optional<TreeNode> snapshotCache;
    private NormalizedNode value;
    private ModificationType modType;

    // Alternative history introduced in WRITE nodes. Instantiated when we touch any child underneath such a node.
    private TreeNode writtenOriginal;

    // Internal cache for TreeNodes created as part of validation
    private ModificationApplyOperation validatedOp;
    private @Nullable TreeNode validatedCurrent;
    private ValidatedTreeNode validatedNode;

    private ModifiedNode(final PathArgument identifier, final @Nullable TreeNode original,
            final ChildTrackingPolicy childPolicy) {
        this.identifier = requireNonNull(identifier);
        this.original = original;
        children = childPolicy.createMap();
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    LogicalOperation getOperation() {
        return operation;
    }

    @Override
    TreeNode original() {
        return original;
    }

    /**
     * Return the value which was written to this node. The returned object is only valid for
     * {@link LogicalOperation#MERGE} and {@link LogicalOperation#WRITE}.
     * operations. It should only be consulted when this modification is going to end up being
     * {@link ModificationType#WRITE}.
     *
     * @return Currently-written value
     */
    @NonNull NormalizedNode getWrittenValue() {
        return verifyNotNull(value);
    }

    /**
     * Returns child modification if child was modified.
     *
     * @return Child modification if direct child or it's subtree was modified.
     */
    @Override
    public ModifiedNode childByArg(final PathArgument arg) {
        return children.get(arg);
    }

    private @Nullable TreeNode metadataFromSnapshot(final @NonNull PathArgument child) {
        final var local = original;
        return local != null ? local.childByArg(child) : null;
    }

    private @Nullable TreeNode metadataFromData(final @NonNull PathArgument child, final Version modVersion) {
        if (writtenOriginal == null) {
            // Lazy instantiation, as we do not want do this for all writes. We are using the modification's version
            // here, as that version is what the SchemaAwareApplyOperation will see when dealing with the resulting
            // modifications.
            writtenOriginal = TreeNode.of(value, modVersion);
        }

        return writtenOriginal.childByArg(child);
    }

    /**
     * Determine the base tree node we are going to apply the operation to. This is not entirely trivial because
     * both DELETE and WRITE operations unconditionally detach their descendants from the original snapshot, so we need
     * to take the current node's operation into account.
     *
     * @param child Child we are looking to modify
     * @param modVersion Version allocated by the calling {@link InMemoryDataTreeModification}
     * @return Before-image tree node as observed by that child.
     */
    private @Nullable TreeNode originalMetadata(final @NonNull PathArgument child, final Version modVersion) {
        return switch (operation) {
            case DELETE ->
                // DELETE implies non-presence
                null;
            case NONE, TOUCH, MERGE -> metadataFromSnapshot(child);
            case WRITE ->
                // WRITE implies presence based on written data
                metadataFromData(child, modVersion);
        };
    }

    /**
     * Returns child modification if child was modified, creates {@link ModifiedNode}
     * for child otherwise. If this node's {@link ModificationType} is {@link ModificationType#UNMODIFIED}
     * changes modification type to {@link ModificationType#SUBTREE_MODIFIED}.
     *
     * @param child child identifier, may not be null
     * @param childOper Child operation
     * @param modVersion Version allocated by the calling {@link InMemoryDataTreeModification}
     * @return {@link ModifiedNode} for specified child, with {@link #getOriginal()}
     *         containing child metadata if child was present in original data.
     */
    ModifiedNode modifyChild(final @NonNull PathArgument child, final @NonNull ModificationApplyOperation childOper,
            final @NonNull Version modVersion) {
        clearSnapshot();
        if (operation == LogicalOperation.NONE) {
            updateOperationType(LogicalOperation.TOUCH);
        }
        final var potential = children.get(child);
        if (potential != null) {
            return potential;
        }

        final var newlyCreated = new ModifiedNode(child, originalMetadata(child, modVersion),
            childOper.getChildPolicy());
        if (operation == LogicalOperation.MERGE && value != null) {
            /*
             * We are attempting to modify a previously-unmodified part of a MERGE node. If the
             * value contains this component, we need to materialize it as a MERGE modification.
             */
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final var childData = ((DistinctNodeContainer) value).childByArg(child);
            if (childData != null) {
                childOper.mergeIntoModifiedNode(newlyCreated, childData, modVersion);
            }
        }

        children.put(child, newlyCreated);
        return newlyCreated;
    }

    /**
     * Returns all recorded direct child modifications.
     *
     * @return all recorded direct child modifications
     */
    @Override
    Collection<ModifiedNode> getChildren() {
        return children.values();
    }

    @Override
    boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * Records a delete for associated node.
     */
    void delete() {
        final LogicalOperation newType = switch (operation) {
            case DELETE, NONE ->
                // We need to record this delete.
                LogicalOperation.DELETE;
            case MERGE ->
                // In case of merge - delete needs to be recored and must not to be changed into NONE, because lazy
                // expansion of parent MERGE node would reintroduce it again.
                LogicalOperation.DELETE;
            case TOUCH, WRITE ->
                // We are canceling a previous modification. This is a bit tricky, as the original write may have just
                // introduced the data, or it may have modified it.
                //
                // As documented in BUG-2470, a delete of data introduced in this transaction needs to be turned into
                // a no-op.
                original != null ? LogicalOperation.DELETE : LogicalOperation.NONE;
        };

        clearSnapshot();
        children.clear();
        value = null;
        updateOperationType(newType);
    }

    /**
     * Records a write for associated node.
     *
     * @param newValue new value
     */
    void write(final NormalizedNode newValue) {
        updateValue(LogicalOperation.WRITE, newValue);
        children.clear();
    }

    /**
     * Seal the modification node and prune any children which has not been modified.
     *
     * @param schema associated apply operation
     * @param version target version
     */
    void seal(final ModificationApplyOperation schema, final Version version) {
        clearSnapshot();
        writtenOriginal = null;

        switch (operation) {
            case TOUCH -> {
                // A TOUCH node without any children is a no-op
                if (children.isEmpty()) {
                    updateOperationType(LogicalOperation.NONE);
                }
            }
            case WRITE -> {
                // A WRITE can collapse all of its children
                if (!children.isEmpty()) {
                    value = schema.apply(this, original(), version).map(TreeNode::getData).orElse(null);
                    children.clear();
                }

                if (value == null) {
                    // The write has ended up being empty, such as a write of an empty list.
                    updateOperationType(LogicalOperation.DELETE);
                } else {
                    schema.fullVerifyStructure(value);
                }
            }
            default -> {
                // No-op
            }
        }
    }

    private void clearSnapshot() {
        snapshotCache = null;
    }

    Optional<TreeNode> getSnapshot() {
        return snapshotCache;
    }

    Optional<TreeNode> setSnapshot(final Optional<TreeNode> snapshot) {
        snapshotCache = requireNonNull(snapshot);
        return snapshot;
    }

    void updateOperationType(final LogicalOperation type) {
        operation = type;
        modType = null;

        // Make sure we do not reuse previously-instantiated data-derived metadata
        writtenOriginal = null;
        clearSnapshot();
    }

    @Override
    public String toString() {
        final ToStringHelper helper = MoreObjects.toStringHelper(this).omitNullValues()
                .add("identifier", identifier).add("operation", operation).add("modificationType", modType);
        if (!children.isEmpty()) {
            helper.add("childModification", children);
        }
        return helper.toString();
    }

    void resolveModificationType(final @NonNull ModificationType type) {
        modType = type;
    }

    /**
     * Update this node's value and operation type without disturbing any of its child modifications.
     *
     * @param type New operation type
     * @param newValue New node value
     */
    void updateValue(final LogicalOperation type, final NormalizedNode newValue) {
        value = requireNonNull(newValue);
        updateOperationType(type);
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

    public static ModifiedNode createUnmodified(final TreeNode metadataTree, final ChildTrackingPolicy childPolicy) {
        return new ModifiedNode(metadataTree.getIdentifier(), requireNonNull(metadataTree), childPolicy);
    }

    void setValidatedNode(final ModificationApplyOperation op, final @Nullable TreeNode currentMeta,
            final Optional<? extends TreeNode> node) {
        validatedOp = requireNonNull(op);
        validatedCurrent = currentMeta;
        validatedNode = new ValidatedTreeNode(node);
    }

    /**
     * Acquire pre-validated node assuming a previous operation and node. This is a counterpart to
     * {@link #setValidatedNode(ModificationApplyOperation, Optional, Optional)}.
     *
     * @param op Currently-executing operation
     * @param storeMeta Currently-used tree node
     * @return {@code null} if there is a mismatch with previously-validated node (if present) or the result of previous
     *         validation.
     */
    @Nullable ValidatedTreeNode validatedNode(final ModificationApplyOperation op, final @Nullable TreeNode storeMeta) {
        return op.equals(validatedOp) && Objects.equals(storeMeta, validatedCurrent) ? validatedNode : null;
    }
}
