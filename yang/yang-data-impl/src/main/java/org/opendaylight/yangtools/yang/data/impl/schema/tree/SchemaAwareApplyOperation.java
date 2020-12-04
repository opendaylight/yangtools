/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class SchemaAwareApplyOperation<T extends WithStatus> extends ModificationApplyOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareApplyOperation.class);

    static ModificationApplyOperation from(final DataSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) throws ExcludedDataSchemaNodeException {
        if (!belongsToTree(treeConfig.getTreeType(), schemaNode)) {
            throw new ExcludedDataSchemaNodeException(schemaNode + " does not belong to configuration tree");
        }
        if (schemaNode instanceof ContainerSchemaNode) {
            return ContainerModificationStrategy.of((ContainerSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof ChoiceSchemaNode) {
            return new ChoiceModificationStrategy((ChoiceSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof LeafListSchemaNode) {
            return MinMaxElementsValidation.from(new LeafSetModificationStrategy((LeafListSchemaNode) schemaNode,
                treeConfig));
        } else if (schemaNode instanceof LeafSchemaNode) {
            return new ValueNodeModificationStrategy<>(LeafNode.class, (LeafSchemaNode) schemaNode);
        } else if (schemaNode instanceof AnydataSchemaNode) {
            return new ValueNodeModificationStrategy<>(AnydataNode.class, (AnydataSchemaNode) schemaNode);
        } else if (schemaNode instanceof AnyxmlSchemaNode) {
            return new ValueNodeModificationStrategy<>(AnyxmlNode.class, (AnyxmlSchemaNode) schemaNode);
        } else if (schemaNode instanceof SchemaContext) {
            return new StructuralContainerModificationStrategy((SchemaContext) schemaNode, treeConfig);
        } else {
            throw new IllegalStateException("Unsupported schema " + schemaNode);
        }
    }

    static AugmentationModificationStrategy from(final DataNodeContainer resolvedTree,
            final AugmentationTarget augSchemas, final AugmentationIdentifier identifier,
            final DataTreeConfiguration treeConfig) {
        for (final AugmentationSchemaNode potential : augSchemas.getAvailableAugmentations()) {
            for (final DataSchemaNode child : potential.getChildNodes()) {
                if (identifier.getPossibleChildNames().contains(child.getQName())) {
                    return new AugmentationModificationStrategy(potential, resolvedTree, treeConfig);
                }
            }
        }

        return null;
    }

    static void checkConflicting(final ModificationPath path, final boolean condition, final String message)
            throws ConflictingModificationAppliedException {
        if (!condition) {
            throw new ConflictingModificationAppliedException(path.toInstanceIdentifier(), message);
        }
    }

    private static ModificationApplyOperation fromListSchemaNode(final ListSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) {
        final List<QName> keyDefinition = schemaNode.getKeyDefinition();
        final SchemaAwareApplyOperation<ListSchemaNode> op;
        if (keyDefinition == null || keyDefinition.isEmpty()) {
            op = new ListModificationStrategy(schemaNode, treeConfig);
        } else {
            op = MapModificationStrategy.of(schemaNode, treeConfig);
        }

        return UniqueValidation.of(schemaNode, treeConfig, MinMaxElementsValidation.from(op));
    }

    protected static void checkNotConflicting(final ModificationPath path, final TreeNode original,
            final TreeNode current) throws ConflictingModificationAppliedException {
        checkConflicting(path, original.getVersion().equals(current.getVersion()),
                "Node was replaced by other transaction.");
        checkConflicting(path, original.getSubtreeVersion().equals(current.getSubtreeVersion()),
                "Node children was modified by other transaction");
    }

    protected final @NonNull ModificationApplyOperation resolveChildOperation(final PathArgument child) {
        final ModificationApplyOperation potential = childByArg(child);
        checkArgument(potential != null, "Operation for child %s is not defined.", child);
        return potential;
    }

    @Override
    final void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        switch (modification.getOperation()) {
            case DELETE:
                checkDeleteApplicable(modification, current);
                break;
            case TOUCH:
                checkTouchApplicable(path, modification, current, version);
                break;
            case WRITE:
                checkWriteApplicable(path, modification, current, version);
                break;
            case MERGE:
                checkMergeApplicable(path, modification, current, version);
                break;
            case NONE:
                break;
            default:
                throw new UnsupportedOperationException(
                    "Suplied modification type " + modification.getOperation() + " is not supported.");
        }
    }

    @Override
    final void quickVerifyStructure(final NormalizedNode<?, ?> writtenValue) {
        verifyValue(writtenValue);
    }

    @Override
    final void fullVerifyStructure(final NormalizedNode<?, ?> writtenValue) {
        verifyValue(writtenValue);
        verifyValueChildren(writtenValue);
    }

    /**
     * Verify the a written value, without performing deeper tree validation.
     *
     * @param writtenValue Written value
     */
    abstract void verifyValue(NormalizedNode<?, ?> writtenValue);

    /**
     * Verify the children implied by a written value after the value itself has been verified by
     * {@link #verifyValue(NormalizedNode)}. Default implementation does nothing.
     *
     * @param writtenValue Written value
     */
    void verifyValueChildren(final NormalizedNode<?, ?> writtenValue) {
        // Defaults to no-op
    }

    protected void checkMergeApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        final Optional<? extends TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            /*
             * We need to do conflict detection only and only if the value of leaf changed
             * before two transactions. If value of leaf is unchanged between two transactions
             * it should not cause transaction to fail, since result of this merge
             * leads to same data.
             */
            final TreeNode orig = original.get();
            final TreeNode cur = current.get();
            if (!orig.getData().equals(cur.getData())) {
                checkNotConflicting(path, orig, cur);
            }
        }
    }

    /**
     * Checks if write operation can be applied to current TreeNode.
     * The operation checks if original tree node to which the modification is going to be applied exists and if
     * current node in TreeNode structure exists.
     *
     * @param path Path from current node in TreeNode
     * @param modification modification to apply
     * @param current current node in TreeNode for modification to apply
     * @throws DataValidationFailedException when a data dependency conflict is detected
     */
    private static void checkWriteApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws DataValidationFailedException {
        final Optional<? extends TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            checkNotConflicting(path, original.get(), current.get());
        } else {
            checkConflicting(path, !original.isPresent(), "Node was deleted by other transaction.");
            checkConflicting(path, !current.isPresent(), "Node was created by other transaction.");
        }
    }

    private static void checkDeleteApplicable(final NodeModification modification,
            final Optional<? extends TreeNode> current) {
        // Delete is always applicable, we do not expose it to subclasses
        if (!current.isPresent()) {
            LOG.trace("Delete operation turned to no-op on missing node {}", modification);
        }
    }

    @Override
    Optional<? extends TreeNode> apply(final ModifiedNode modification, final Optional<? extends TreeNode> currentMeta,
            final Version version) {
        switch (modification.getOperation()) {
            case DELETE:
                // Deletion of a non-existing node is a no-op, report it as such
                modification.resolveModificationType(currentMeta.isPresent() ? ModificationType.DELETE
                        : ModificationType.UNMODIFIED);
                return modification.setSnapshot(Optional.empty());
            case TOUCH:
                checkArgument(currentMeta.isPresent(), "Metadata not available for modification %s", modification);
                return modification.setSnapshot(Optional.of(applyTouch(modification, currentMeta.get(),
                    version)));
            case MERGE:
                final TreeNode result;

                if (!currentMeta.isPresent()) {
                    // This is a slight optimization: a merge on a non-existing node equals to a write. Written data
                    // structure is usually verified when the transaction is sealed. To preserve correctness, we have
                    // to run that validation here.
                    modification.resolveModificationType(ModificationType.WRITE);
                    result = applyWrite(modification, modification.getWrittenValue(), currentMeta, version);
                    fullVerifyStructure(result.getData());
                } else {
                    result = applyMerge(modification, currentMeta.get(), version);
                }

                return modification.setSnapshot(Optional.of(result));
            case WRITE:
                modification.resolveModificationType(ModificationType.WRITE);
                return modification.setSnapshot(Optional.of(applyWrite(modification,
                    verifyNotNull(modification.getWrittenValue()), currentMeta, version)));
            case NONE:
                modification.resolveModificationType(ModificationType.UNMODIFIED);
                return currentMeta;
            default:
                throw new IllegalArgumentException("Provided modification type is not supported.");
        }
    }

    /**
     * Apply a merge operation. Since the result of merge differs based on the data type
     * being modified, implementations of this method are responsible for calling
     * {@link ModifiedNode#resolveModificationType(ModificationType)} as appropriate.
     *
     * @param modification Modified node
     * @param currentMeta Store Metadata Node on which NodeModification should be applied
     * @param version New subtree version of parent node
     * @return A sealed TreeNode representing applied operation.
     */
    protected abstract TreeNode applyMerge(ModifiedNode modification, TreeNode currentMeta, Version version);

    protected abstract TreeNode applyWrite(ModifiedNode modification, NormalizedNode<?, ?> newValue,
            Optional<? extends TreeNode> currentMeta, Version version);

    /**
     * Apply a nested operation. Since there may not actually be a nested operation
     * to be applied, implementations of this method are responsible for calling
     * {@link ModifiedNode#resolveModificationType(ModificationType)} as appropriate.
     *
     * @param modification Modified node
     * @param currentMeta Store Metadata Node on which NodeModification should be applied
     * @param version New subtree version of parent node
     * @return A sealed TreeNode representing applied operation.
     */
    protected abstract TreeNode applyTouch(ModifiedNode modification, TreeNode currentMeta, Version version);

    /**
     * Checks is supplied {@link NodeModification} is applicable for Subtree Modification.
     *
     * @param path Path to current node
     * @param modification Node modification which should be applied.
     * @param current Current state of data tree
     * @throws ConflictingModificationAppliedException If subtree was changed in conflicting way
     * @throws org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException If subtree
     *         modification is not applicable (e.g. leaf node).
     */
    protected abstract void checkTouchApplicable(ModificationPath path, NodeModification modification,
            Optional<? extends TreeNode> current, Version version) throws DataValidationFailedException;

    /**
     * Return the {@link WithStatus}-subclass schema associated with this operation.
     * @return A model node
     */
    abstract @NonNull T getSchema();

    /**
     * Checks if supplied schema node belong to specified Data Tree type. All nodes belong to the operational tree,
     * nodes in configuration tree are marked as such.
     *
     * @param treeType Tree Type
     * @param node Schema node
     * @return {@code true} if the node matches the tree type, {@code false} otherwise.
     */
    static final boolean belongsToTree(final TreeType treeType, final DataSchemaNode node) {
        return treeType == TreeType.OPERATIONAL || node.effectiveConfig().orElse(Boolean.TRUE);
    }
}
