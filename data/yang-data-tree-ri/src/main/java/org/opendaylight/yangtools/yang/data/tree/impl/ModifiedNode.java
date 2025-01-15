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
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * <p>Tree which structurally resembles data tree and captures client modifications to the data store tree. This tree is
 * lazily created and populated via {@link #modifyChild(PathArgument, ModificationApplyOperation, Version)} and
 * {@link TreeNode} which represents original state as tracked by {@link #original()}.
 *
 * <p>The contract is that the state information exposed here preserves the temporal ordering of whatever modifications
 * were executed. A child's effects pertain to data node as modified by its ancestors. This means that in order to
 * reconstruct the effective data node presentation, it is sufficient to perform a depth-first pre-order traversal of
 * the tree.
 */
final class ModifiedNode extends NodeModification implements StoreTreeNode<ModifiedNode> {
    // This class has rather funky state management and thread safety rules, closely aligned with
    // InMemoryDataTreeModification's lifecycle, which holds the pointer to the root of a tree of ModifiedNodes. Here
    // we document our fields, but all inter-thread synchronization needs to be done in InMemoryDataTreeModification.
    //
    // Every ModifiedNode has two invariants, guaranteed to be immutable throughout its existence:
    // - 'identifier', which is the PathArgument identifying the underlying NormalizedNode data
    // - 'original', which is the TreeNode which is being modified -- or null if this is a modification of previously
    //   non-existent data
    private final @Nullable TreeNode original;
    private final @NonNull PathArgument identifier;

    // Two other bits start off as potentially-mutable state:
    // - 'operation', which is the LogicalOperation this ModifiedNode performs on the corresponding data
    // - 'children', which tracks any nested modifications
    //
    // Both start as being mutable and assuming non-concurrent access, as is the case when a DataTreeModification is
    // being built up from its constituent logical operations.
    //
    // Once DataTreeModification.ready() is invoked by the user we process the subtree, perform initial logical
    // operation algebra and prune the tree so it records the effective logical operations the user intends to perform
    // on top of 'original' -- see the seal() method below.
    //
    // After this process completes, the following four fields are guaranteed to remain stable.
    private @NonNull Map<PathArgument, ModifiedNode> children;
    private @NonNull LogicalOperation operation;

    // The argument to LogicalOperation.{MERGE,WRITE}, invalid otherwise
    private NormalizedNode value;
    // Alternative history introduced in WRITE nodes. Instantiated when we touch any child underneath such a node.
    private TreeNode writtenOriginal;

    // Cached result of the last SchemaAwareApplyOperation.apply() operation, for example if the user calls
    // DataTreeSnapshot.readNode() multiple times on the same node.
    private Optional<TreeNode> snapshotCache;

    // Effective ModificationType, as resolved by the last executed SchemaAwareApplyOperation.apply()
    private ModificationType modType;
    private Map<PathArgument, ModifiedNode> applyChildren;

    // Internal cache for TreeNodes created as part of validation
    private ModificationApplyOperation validatedOp;
    private @Nullable TreeNode validatedCurrent;
    private ValidatedTreeNode validatedNode;

    private ModifiedNode(final PathArgument identifier, final @Nullable TreeNode original,
            final ChildTrackingPolicy childPolicy) {
        this.identifier = requireNonNull(identifier);
        this.original = original;
        children = childPolicy.createMap();
        operation = LogicalOperation.NONE;
    }

    ModifiedNode(final ModifiedNode prev, final ChildTrackingPolicy childPolicy) {
        identifier = prev.identifier;
        original = prev.original;
        children = childPolicy.createMap();
        operation = prev.operation;

        value = prev.value;
        writtenOriginal = prev.writtenOriginal;
        children.putAll(prev.children);
    }

    ModifiedNode(final TreeNode metadataTree, final ChildTrackingPolicy childPolicy) {
        this(metadataTree.data().name(), metadataTree, childPolicy);
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

    @Override
    NormalizedNode getValue() {
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
     * @return {@link ModifiedNode} for specified child, with {@link #original()} containing child metadata if child was
     *         present in original data.
     */
    @NonNullByDefault
    ModifiedNode modifyChild(final PathArgument child, final ModificationApplyOperation childOper,
            final Version modVersion) {
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
            // We are attempting to modify a previously-unmodified part of a MERGE node. If the value contains this
            // component, we need to materialize it as a MERGE modification.
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final var childData = ((DistinctNodeContainer) value).childByArg(child);
            if (childData != null) {
                childOper.mergeIntoModifiedNode(newlyCreated, childData, modVersion);
            }
        }

        children.put(child, newlyCreated);
        return newlyCreated;
    }

    @NonNullByDefault
    ModifiedNode createMergeChild(final NormalizedNode child, final ModificationApplyOperation childOper,
            final Version modVersion) {
        final var name = child.name();
        final var node = new ModifiedNode(name, originalMetadata(name, modVersion), childOper.getChildPolicy());
        childOper.mergeIntoModifiedNode(node, child, modVersion);

        final var existing = children.putIfAbsent(name, node);
        if (existing != null) {
            throw new VerifyException("Attempted to replace " + existing + " with " + node);
        }
        return node;
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
        final var newType = switch (operation) {
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

        var isEmpty = children.isEmpty();

        switch (operation) {
            case TOUCH -> {
                // A TOUCH node without any children is a no-op
                if (isEmpty) {
                    updateOperationType(LogicalOperation.NONE);
                }
            }
            case WRITE -> {
                // A WRITE can collapse all of its children
                if (!isEmpty) {
                    final var applied = schema.apply(this, original(), version);
                    value = applied != null ? applied.data() : null;
                    isEmpty = true;
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

        if (isEmpty) {
            //
            children = Map.of();
        }
    }

    private void clearSnapshot() {
        snapshotCache = null;
    }

    Optional<TreeNode> getSnapshot() {
        return snapshotCache;
    }

    @Nullable TreeNode setSnapshot(final @Nullable TreeNode snapshot) {
        snapshotCache = Optional.ofNullable(snapshot);
        return snapshot;
    }

    void updateOperationType(final LogicalOperation type) {
        operation = requireNonNull(type);
        modType = null;
        applyChildren = null;

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
        modType = requireNonNull(type);
        applyChildren = ImmutableMap.of();
    }

    @NonNullByDefault
    void resolveModificationType(final ModifiedNode source, final List<ModifiedNode> extraChildren) {
        modType = source.modType;
        snapshotCache = source.snapshotCache;
        validatedOp = source.validatedOp;
        validatedCurrent = source.validatedCurrent;
        validatedNode = source.validatedNode;

        applyChildren = extraChildren.stream()
            .filter(node -> node.getModificationType() != ModificationType.UNMODIFIED)
            .collect(ImmutableMap.toImmutableMap(ModifiedNode::getIdentifier, Function.identity()));
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

    @Nullable ModifiedNode modifiedChild(final PathArgument childName) {
        final var child = children.get(childName);
        return child != null ? child : applyChildren.get(childName);
    }

    Collection<ModifiedNode> modifiedChildren() {
        final var normal = getChildren();
        if (applyChildren.isEmpty()) {
            return normal;
        }

        final var apply = applyChildren.values();
        return normal.isEmpty() ? apply : new AbstractCollection<>() {
            @Override
            public Iterator<ModifiedNode> iterator() {
                return Iterators.concat(normal.iterator(), apply.iterator());
            }

            @Override
            public int size() {
                return normal.size() + apply.size();
            }
        };
    }

    void setValidatedNode(final ModificationApplyOperation op, final @Nullable TreeNode currentMeta,
            final @Nullable TreeNode node) {
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
