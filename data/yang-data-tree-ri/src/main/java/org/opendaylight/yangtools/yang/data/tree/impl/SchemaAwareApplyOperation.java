/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.spi.node.MandatoryLeafEnforcer;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract sealed class SchemaAwareApplyOperation<T extends DataSchemaNode> extends ModificationApplyOperation
        permits AbstractNodeContainerModificationStrategy, ListModificationStrategy, ValueNodeModificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareApplyOperation.class);
    static final @NonNull BuilderFactory BUILDER_FACTORY = ImmutableNodes.builderFactory();

    static ModificationApplyOperation from(final DataSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) throws ExcludedDataSchemaNodeException {
        if (!belongsToTree(treeConfig.getTreeType(), schemaNode)) {
            throw new ExcludedDataSchemaNodeException(schemaNode + " does not belong to configuration tree");
        }
        if (schemaNode instanceof ContainerSchemaNode container) {
            return ContainerModificationStrategy.of(container, treeConfig);
        } else if (schemaNode instanceof ListSchemaNode list) {
            return fromListSchemaNode(list, treeConfig);
        } else if (schemaNode instanceof ChoiceSchemaNode choice) {
            return new ChoiceModificationStrategy(choice, treeConfig);
        } else if (schemaNode instanceof LeafListSchemaNode leafList) {
            return MinMaxElementsValidation.from(new LeafSetModificationStrategy(leafList, treeConfig));
        } else if (schemaNode instanceof LeafSchemaNode leaf) {
            return new ValueNodeModificationStrategy<>(LeafNode.class, leaf);
        } else if (schemaNode instanceof AnydataSchemaNode anydata) {
            return new ValueNodeModificationStrategy<>(AnydataNode.class, anydata);
        } else if (schemaNode instanceof AnyxmlSchemaNode anyxml) {
            return new ValueNodeModificationStrategy<>(AnyxmlNode.class, anyxml);
        } else if (schemaNode instanceof SchemaContext context) {
            return new ContainerModificationStrategy.Structural(context, treeConfig);
        } else {
            throw new IllegalStateException("Unsupported schema " + schemaNode);
        }
    }

    static void checkConflicting(final ModificationPath path, final boolean condition, final String message)
            throws ConflictingModificationAppliedException {
        if (!condition) {
            throw new ConflictingModificationAppliedException(path.toInstanceIdentifier(), message);
        }
    }

    private static ModificationApplyOperation fromListSchemaNode(final ListSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) {
        final var keyDefinition = schemaNode.getKeyDefinition();
        final var strategy = keyDefinition == null || keyDefinition.isEmpty()
            ? new ListModificationStrategy(schemaNode, treeConfig)
                : MapModificationStrategy.of(schemaNode, treeConfig);
        return UniqueValidation.of(schemaNode, treeConfig, MinMaxElementsValidation.from(strategy));
    }

    protected static final void checkNotConflicting(final ModificationPath path, final @NonNull TreeNode original,
            final @NonNull TreeNode current) throws ConflictingModificationAppliedException {
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
            final TreeNode currentMeta, final Version version) throws DataValidationFailedException {
        switch (modification.getOperation()) {
            case DELETE -> checkDeleteApplicable(modification, currentMeta);
            case TOUCH -> checkTouchApplicable(path, modification, currentMeta, version);
            case WRITE -> checkWriteApplicable(path, modification, currentMeta, version);
            case MERGE -> checkMergeApplicable(path, modification, currentMeta, version);
            case NONE -> {
                // No-op
            }
            default -> throw new UnsupportedOperationException(
                "Suplied modification type " + modification.getOperation() + " is not supported.");
        }
    }

    @Override
    final void quickVerifyStructure(final NormalizedNode writtenValue) {
        verifyValue(writtenValue);
    }

    @Override
    final void fullVerifyStructure(final NormalizedNode writtenValue) {
        verifyValue(writtenValue);
        verifyValueChildren(writtenValue);
    }

    /**
     * Verify the a written value, without performing deeper tree validation.
     *
     * @param writtenValue Written value
     */
    abstract void verifyValue(NormalizedNode writtenValue);

    /**
     * Verify the children implied by a written value after the value itself has been verified by
     * {@link #verifyValue(NormalizedNode)}. Default implementation does nothing.
     *
     * @param writtenValue Written value
     */
    void verifyValueChildren(final NormalizedNode writtenValue) {
        // Defaults to no-op
    }

    protected void checkMergeApplicable(final ModificationPath path, final NodeModification modification,
            final TreeNode currentMeta, final Version version) throws DataValidationFailedException {
        final var orig = modification.original();
        if (orig != null && currentMeta != null) {
            /*
             * We need to do conflict detection only and only if the value of leaf changed before two transactions. If
             * value of leaf is unchanged between two transactions it should not cause transaction to fail, since result
             * of this merge leads to same data.
             */
            if (!orig.getData().equals(currentMeta.getData())) {
                checkNotConflicting(path, orig, currentMeta);
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
     * @param currentMeta current node in TreeNode for modification to apply
     * @throws DataValidationFailedException when a data dependency conflict is detected
     */
    private static void checkWriteApplicable(final ModificationPath path, final NodeModification modification,
            final TreeNode currentMeta, final Version version) throws DataValidationFailedException {
        final var original = modification.original();
        if (original != null && currentMeta != null) {
            checkNotConflicting(path, original, currentMeta);
        } else {
            checkConflicting(path, original == null, "Node was deleted by other transaction.");
            checkConflicting(path, currentMeta == null, "Node was created by other transaction.");
        }
    }

    private static void checkDeleteApplicable(final NodeModification modification,
            final @Nullable TreeNode currentMeta) {
        // Delete is always applicable, we do not expose it to subclasses
        if (currentMeta == null) {
            LOG.trace("Delete operation turned to no-op on missing node {}", modification);
        }
    }

    @Override
    TreeNode apply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return switch (modification.getOperation()) {
            case DELETE -> {
                // Deletion of a non-existing node is a no-op, report it as such
                modification.resolveModificationType(currentMeta != null ? ModificationType.DELETE
                        : ModificationType.UNMODIFIED);
                yield modification.setSnapshot(null);
            }
            case TOUCH -> {
                if (currentMeta == null) {
                    throw new IllegalArgumentException("Metadata not available for modification " + modification);
                }
                yield modification.setSnapshot(applyTouch(modification, currentMeta, version));
            }
            case MERGE -> {
                final TreeNode result;

                if (currentMeta == null) {
                    // This is a slight optimization: a merge on a non-existing node equals to a write. Written data
                    // structure is usually verified when the transaction is sealed. To preserve correctness, we have
                    // to run that validation here.
                    modification.resolveModificationType(ModificationType.WRITE);
                    result = applyWrite(modification, modification.getWrittenValue(), null, version);
                    fullVerifyStructure(result.getData());
                } else {
                    result = applyMerge(modification, currentMeta, version);
                }

                yield modification.setSnapshot(result);
            }
            case WRITE -> {
                modification.resolveModificationType(ModificationType.WRITE);
                yield modification.setSnapshot(applyWrite(modification, verifyNotNull(modification.getWrittenValue()),
                    currentMeta, version));
            }
            case NONE -> {
                modification.resolveModificationType(ModificationType.UNMODIFIED);
                yield currentMeta;
            }
        };
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
    protected abstract @NonNull TreeNode applyMerge(ModifiedNode modification, @NonNull TreeNode currentMeta,
        Version version);

    protected abstract @NonNull TreeNode applyWrite(ModifiedNode modification, NormalizedNode newValue,
        @Nullable TreeNode currentMeta, Version version);

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
    protected abstract @NonNull TreeNode applyTouch(ModifiedNode modification, @NonNull TreeNode currentMeta,
        Version version);

    /**
     * Checks is supplied {@link NodeModification} is applicable for Subtree Modification.
     *
     * @param path Path to current node
     * @param modification Node modification which should be applied.
     * @param currentMeta Current state of data tree
     * @throws ConflictingModificationAppliedException If subtree was changed in conflicting way
     * @throws org.opendaylight.yangtools.yang.data.tree.api.IncorrectDataStructureException If subtree
     *         modification is not applicable (e.g. leaf node).
     */
    protected abstract void checkTouchApplicable(ModificationPath path, NodeModification modification,
        @Nullable TreeNode currentMeta, Version version) throws DataValidationFailedException;

    /**
     * Return the {@link DataSchemaNode}-subclass schema associated with this operation.
     *
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

    static final @Nullable MandatoryLeafEnforcer enforcerFor(final DataSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        if (treeConfig.isMandatoryNodesValidationEnabled() && schema instanceof DataNodeContainer container) {
            final var includeConfigFalse = treeConfig.getTreeType() == TreeType.OPERATIONAL;
            if (includeConfigFalse || schema.effectiveConfig().orElse(Boolean.TRUE)) {
                return MandatoryLeafEnforcer.forContainer(container, includeConfigFalse);
            }
        }
        return null;
    }
}
