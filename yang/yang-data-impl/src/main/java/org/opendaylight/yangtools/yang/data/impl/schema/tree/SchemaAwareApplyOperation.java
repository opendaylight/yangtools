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
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class SchemaAwareApplyOperation extends ModificationApplyOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareApplyOperation.class);

    public static ModificationApplyOperation from(final DataSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) {
        if (treeConfig.getTreeType() == TreeType.CONFIGURATION) {
            Preconditions.checkArgument(schemaNode.isConfiguration(),
                "Supplied %s does not belongs to configuration tree.", schemaNode.getPath());
        }
        if (schemaNode instanceof ContainerSchemaNode) {
            final ContainerSchemaNode containerSchema = (ContainerSchemaNode) schemaNode;
            if (containerSchema.isPresenceContainer()) {
                return new PresenceContainerModificationStrategy(containerSchema, treeConfig);
            }
            return new StructuralContainerModificationStrategy(containerSchema, treeConfig);
        } else if (schemaNode instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof ChoiceSchemaNode) {
            return new ChoiceModificationStrategy((ChoiceSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof LeafListSchemaNode) {
            return fromLeafListSchemaNode((LeafListSchemaNode) schemaNode, treeConfig);
        } else if (schemaNode instanceof LeafSchemaNode) {
            return new LeafModificationStrategy((LeafSchemaNode) schemaNode);
        }
        throw new IllegalArgumentException("Not supported schema node type for " + schemaNode.getClass());
    }

    public static SchemaAwareApplyOperation from(final DataNodeContainer resolvedTree,
            final AugmentationTarget augSchemas, final AugmentationIdentifier identifier,
            final DataTreeConfiguration treeConfig) {
        for (final AugmentationSchema potential : augSchemas.getAvailableAugmentations()) {
            for (final DataSchemaNode child : potential.getChildNodes()) {
                if (identifier.getPossibleChildNames().contains(child.getQName())) {
                    return new AugmentationModificationStrategy(potential, resolvedTree, treeConfig);
                }
            }
        }

        return null;
    }

    public static void checkConflicting(final YangInstanceIdentifier path, final boolean condition,
                                        final String message) throws ConflictingModificationAppliedException {
        if (!condition) {
            throw new ConflictingModificationAppliedException(path, message);
        }
    }

    private static SchemaAwareApplyOperation fromListSchemaNode(final ListSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) {
        final List<QName> keyDefinition = schemaNode.getKeyDefinition();
        final SchemaAwareApplyOperation op;
        if (keyDefinition == null || keyDefinition.isEmpty()) {
            op = new UnkeyedListModificationStrategy(schemaNode, treeConfig);
        } else if (schemaNode.isUserOrdered()) {
            op =  new OrderedMapModificationStrategy(schemaNode, treeConfig);
        } else {
            op = new UnorderedMapModificationStrategy(schemaNode, treeConfig);
        }
        return MinMaxElementsValidation.from(op, schemaNode);
    }

    private static SchemaAwareApplyOperation fromLeafListSchemaNode(final LeafListSchemaNode schemaNode,
            final DataTreeConfiguration treeConfig) {
        final SchemaAwareApplyOperation op;
        if (schemaNode.isUserOrdered()) {
            op =  new OrderedLeafSetModificationStrategy(schemaNode, treeConfig);
        } else {
            op = new UnorderedLeafSetModificationStrategy(schemaNode, treeConfig);
        }
        return MinMaxElementsValidation.from(op, schemaNode);
    }

    protected static void checkNotConflicting(final YangInstanceIdentifier path, final TreeNode original,
            final TreeNode current) throws ConflictingModificationAppliedException {
        checkConflicting(path, original.getVersion().equals(current.getVersion()),
                "Node was replaced by other transaction.");
        checkConflicting(path, original.getSubtreeVersion().equals(current.getSubtreeVersion()),
                "Node children was modified by other transaction");
    }

    protected final ModificationApplyOperation resolveChildOperation(final PathArgument child) {
        final Optional<ModificationApplyOperation> potential = getChild(child);
        Preconditions.checkArgument(potential.isPresent(), "Operation for child %s is not defined.", child);
        return potential.get();
    }

    @Override
    final void checkApplicable(final YangInstanceIdentifier path,final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
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

    protected void checkMergeApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        final Optional<TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            /*
             * We need to do conflict detection only and only if the value of leaf changed
             * before two transactions. If value of leaf is unchanged between two transactions
             * it should not cause transaction to fail, since result of this merge
             * leads to same data.
             */
            if (!original.get().getData().equals(current.get().getData())) {
                checkNotConflicting(path, original.get(), current.get());
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
    protected void checkWriteApplicable(final YangInstanceIdentifier path, final NodeModification modification,
        final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        final Optional<TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            checkNotConflicting(path, original.get(), current.get());
        } else if (original.isPresent()) {
            throw new ConflictingModificationAppliedException(path, "Node was deleted by other transaction.");
        } else if (current.isPresent()) {
            throw new ConflictingModificationAppliedException(path, "Node was created by other transaction.");
        }
    }

    private static void checkDeleteApplicable(final NodeModification modification, final Optional<TreeNode> current) {
        // Delete is always applicable, we do not expose it to subclasses
        if (!current.isPresent()) {
            LOG.trace("Delete operation turned to no-op on missing node {}", modification);
        }
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.UNORDERED;
    }

    @Override
    final Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        switch (modification.getOperation()) {
            case DELETE:
                // Deletion of a non-existing node is a no-op, report it as such
                modification.resolveModificationType(currentMeta.isPresent() ? ModificationType.DELETE
                        : ModificationType.UNMODIFIED);
                return modification.setSnapshot(Optional.absent());
            case TOUCH:
                Preconditions.checkArgument(currentMeta.isPresent(), "Metadata not available for modification %s",
                    modification);
                return modification.setSnapshot(Optional.of(applyTouch(modification, currentMeta.get(),
                    version)));
            case MERGE:
                final TreeNode result;

                if (!currentMeta.isPresent()) {
                    // This is a slight optimization: a merge on a non-existing node equals to a write. Written data
                    // structure is usually verified when the transaction is sealed. To preserve correctness, we have
                    // to run that validation here.
                    modification.resolveModificationType(ModificationType.WRITE);
                    result = applyWrite(modification, currentMeta, version);
                    verifyStructure(result.getData(), true);
                } else {
                    result = applyMerge(modification, currentMeta.get(), version);
                }

                return modification.setSnapshot(Optional.of(result));
            case WRITE:
                modification.resolveModificationType(ModificationType.WRITE);
                return modification.setSnapshot(Optional.of(applyWrite(modification, currentMeta, version)));
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

    protected abstract TreeNode applyWrite(ModifiedNode modification, Optional<TreeNode> currentMeta, Version version);

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
     *
     * Checks is supplied {@link NodeModification} is applicable for Subtree Modification.
     *
     * @param path Path to current node
     * @param modification Node modification which should be applied.
     * @param current Current state of data tree
     * @throws ConflictingModificationAppliedException If subtree was changed in conflicting way
     * @throws org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException If subtree
     *         modification is not applicable (e.g. leaf node).
     */
    protected abstract void checkTouchApplicable(YangInstanceIdentifier path, NodeModification modification,
            Optional<TreeNode> current, Version version) throws DataValidationFailedException;

    /**
     * Checks if supplied schema node belong to specified Data Tree type. All nodes belong to the operational tree,
     * nodes in configuration tree are marked as such.
     *
     * @param treeType Tree Type
     * @param node Schema node
     * @return {@code true} if the node matches the tree type, {@code false} otherwise.
     */
    static boolean belongsToTree(final TreeType treeType, final DataSchemaNode node) {
        return treeType == TreeType.OPERATIONAL || node.isConfiguration();
    }
}
