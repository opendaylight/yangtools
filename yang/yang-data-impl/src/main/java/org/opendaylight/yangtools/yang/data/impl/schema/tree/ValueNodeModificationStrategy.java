/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ValueNodeModificationStrategy<T extends DataSchemaNode> extends SchemaAwareApplyOperation<T> {
    private final Class<? extends ValueNode> nodeClass;
    private final @NonNull T schema;

    ValueNodeModificationStrategy(final Class<? extends ValueNode> nodeClass, final T schema) {
        this.nodeClass = requireNonNull(nodeClass);
        this.schema = requireNonNull(schema);
    }

    @Override
    T getSchema() {
        return schema;
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        throw new UnsupportedOperationException("Node " + schema.getPath()
                + " is leaf type node. Child nodes not allowed");
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.NONE;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        throw new UnsupportedOperationException("Node " + schema.getPath()
                + " is leaf type node. Subtree change is not allowed.");
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        // Just overwrite whatever was there, but be sure to run validation
        final NormalizedNode<?, ?> newValue = modification.getWrittenValue();
        verifyWrittenValue(newValue);
        modification.resolveModificationType(ModificationType.WRITE);
        return applyWrite(modification, newValue, null, version);
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode<?, ?> newValue,
            final Optional<TreeNode> currentMeta, final Version version) {
        return TreeNodeFactory.createTreeNode(newValue, version);
    }

    @Override
    protected void checkTouchApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path.toInstanceIdentifier(), "Subtree modification is not allowed.");
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        switch (node.getOperation()) {
            // Delete performs a data dependency check on existence of the node. Performing a merge
            // on DELETE means we
            // are really performing a write.
            case DELETE:
            case WRITE:
                node.write(value);
                break;
            default:
                node.updateValue(LogicalOperation.MERGE, value);
        }
    }

    @Override
    void verifyValue(final NormalizedNode<?, ?> writtenValue) {
        verifyWrittenValue(writtenValue);
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        verifyWrittenValue(value);
    }

    private void verifyWrittenValue(final NormalizedNode<?, ?> value) {
        checkArgument(nodeClass.isInstance(value), "Expected an instance of %s, have %s", nodeClass, value);
    }
}
