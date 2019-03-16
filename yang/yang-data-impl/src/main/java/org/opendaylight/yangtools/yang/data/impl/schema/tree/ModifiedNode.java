/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

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
    static final Predicate<ModifiedNode> IS_TERMINAL_PREDICATE = input -> {
        requireNonNull(input);
        switch (input.getOperation()) {
            case DELETE:
            case MERGE:
            case WRITE:
                return true;
            case TOUCH:
            case NONE:
                return false;
            default:
                throw new IllegalArgumentException("Unhandled modification type " + input.getOperation());
        }
    };

    private final Map<PathArgument, ModifiedNode> children;
    private final Optional<TreeNode> original;
    private final PathArgument identifier;
    private LogicalOperation operation = LogicalOperation.NONE;
    private Optional<TreeNode> snapshotCache;
    private NormalizedNode<?, ?> value;
    private ModificationType modType;

    // Alternative history introduced in WRITE nodes. Instantiated when we touch any child underneath such a node.
    private TreeNode writtenOriginal;

    // Internal cache for TreeNodes created as part of validation
    private ModificationApplyOperation validatedOp;
    private Optional<TreeNode> validatedCurrent;
    private Optional<TreeNode> validatedNode;

    private ModifiedNode(final PathArgument identifier, final Optional<TreeNode> original,
            final ChildTrackingPolicy childPolicy) {
        this.identifier = identifier;
        this.original = original;
        this.children = childPolicy.createMap();
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
    Optional<TreeNode> getOriginal() {
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
    @NonNull NormalizedNode<?, ?> getWrittenValue() {
        return verifyNotNull(value);
    }

    /**
     * Returns child modification if child was modified.
     *
     * @return Child modification if direct child or it's subtree was modified.
     */
    @Override
    public Optional<ModifiedNode> getChild(final PathArgument child) {
        return Optional.ofNullable(children.get(child));
    }

    private Optional<TreeNode> metadataFromSnapshot(final @NonNull PathArgument child) {
        return original.isPresent() ? original.get().getChild(child) : Optional.empty();
    }

    private Optional<TreeNode> metadataFromData(final @NonNull PathArgument child, final Version modVersion) {
        if (writtenOriginal == null) {
            // Lazy instantiation, as we do not want do this for all writes. We are using the modification's version
            // here, as that version is what the SchemaAwareApplyOperation will see when dealing with the resulting
            // modifications.
            writtenOriginal = TreeNodeFactory.createTreeNode(value, modVersion);
        }

        return writtenOriginal.getChild(child);
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
    private Optional<TreeNode> findOriginalMetadata(final @NonNull PathArgument child, final Version modVersion) {
        switch (operation) {
            case DELETE:
                // DELETE implies non-presence
                return Optional.empty();
            case NONE:
            case TOUCH:
            case MERGE:
                return metadataFromSnapshot(child);
            case WRITE:
                // WRITE implies presence based on written data
                return metadataFromData(child, modVersion);
            default:
                throw new IllegalStateException("Unhandled node operation " + operation);
        }
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
        final ModifiedNode potential = children.get(child);
        if (potential != null) {
            return potential;
        }

        final Optional<TreeNode> currentMetadata = findOriginalMetadata(child, modVersion);


        final ModifiedNode newlyCreated = new ModifiedNode(child, currentMetadata, childOper.getChildPolicy());
        if (operation == LogicalOperation.MERGE && value != null) {
            /*
             * We are attempting to modify a previously-unmodified part of a MERGE node. If the
             * value contains this component, we need to materialize it as a MERGE modification.
             */
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final Optional<NormalizedNode<?, ?>> childData = ((NormalizedNodeContainer)value).getChild(child);
            if (childData.isPresent()) {
                childOper.mergeIntoModifiedNode(newlyCreated, childData.get(), modVersion);
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
                // In case of merge - delete needs to be recored and must not to be changed into NONE, because lazy
                // expansion of parent MERGE node would reintroduce it again.
                newType = LogicalOperation.DELETE;
                break;
            case TOUCH:
            case WRITE:
                /*
                 * We are canceling a previous modification. This is a bit tricky, as the original write may have just
                 * introduced the data, or it may have modified it.
                 *
                 * As documented in BUG-2470, a delete of data introduced in this transaction needs to be turned into
                 * a no-op.
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
     * @param newValue new value
     */
    void write(final NormalizedNode<?, ?> newValue) {
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
            case TOUCH:
                // A TOUCH node without any children is a no-op
                if (children.isEmpty()) {
                    updateOperationType(LogicalOperation.NONE);
                }
                break;
            case WRITE:
                // A WRITE can collapse all of its children
                if (!children.isEmpty()) {
                    value = schema.apply(this, getOriginal(), version).map(TreeNode::getData).orElse(null);
                    children.clear();
                }

                if (value == null) {
                    // The write has ended up being empty, such as a write of an empty list.
                    updateOperationType(LogicalOperation.DELETE);
                } else {
                    schema.fullVerifyStructure(value);
                }
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
    void updateValue(final LogicalOperation type, final NormalizedNode<?, ?> newValue) {
        this.value = requireNonNull(newValue);
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
        return new ModifiedNode(metadataTree.getIdentifier(), Optional.of(metadataTree), childPolicy);
    }

    void setValidatedNode(final ModificationApplyOperation op, final Optional<TreeNode> current,
            final Optional<TreeNode> node) {
        this.validatedOp = requireNonNull(op);
        this.validatedCurrent = requireNonNull(current);
        this.validatedNode = requireNonNull(node);
    }

    Optional<TreeNode> getValidatedNode(final ModificationApplyOperation op, final Optional<TreeNode> current) {
        return op.equals(validatedOp) && current.equals(validatedCurrent) ? validatedNode : null;
    }
}
