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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ValueNodeModificationStrategy<T extends DataSchemaNode, V extends NormalizedNode>
        extends SchemaAwareApplyOperation<T> {
    private final @NonNull Class<V> nodeClass;
    private final @NonNull T schema;

    ValueNodeModificationStrategy(final Class<V> nodeClass, final T schema) {
        this.nodeClass = requireNonNull(nodeClass);
        this.schema = requireNonNull(schema);
    }

    @Override
    T getSchema() {
        return schema;
    }

    @Override
    public ModificationApplyOperation childByArg(final PathArgument arg) {
        throw new UnsupportedOperationException("Node " + schema + " is leaf type node. Child nodes not allowed");
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.NONE;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        throw new UnsupportedOperationException("Node " + schema + " is leaf type node. "
            + " Subtree change is not allowed.");
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        // Just overwrite whatever was there, but be sure to run validation
        final NormalizedNode newValue = modification.getWrittenValue();
        verifyWrittenValue(newValue);
        modification.resolveModificationType(ModificationType.WRITE);
        return applyWrite(modification, newValue, null, version);
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final Optional<? extends TreeNode> currentMeta, final Version version) {
        return newTreeNode(newValue, version);
    }

    @Override
    protected void checkTouchApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<? extends TreeNode> current, final Version version) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path.toInstanceIdentifier(), "Subtree modification is not allowed.");
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode value, final Version version) {
        switch (node.getOperation()) {
            // Delete performs a data dependency check on existence of the node. Performing a merge on DELETE means we
            // are really performing a write.
            case DELETE, WRITE -> node.write(value);
            default -> node.updateValue(LogicalOperation.MERGE, value);
        }
    }

    @Override
    void verifyValue(final NormalizedNode writtenValue) {
        verifyWrittenValue(writtenValue);
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode value) {
        verifyWrittenValue(value);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("value", nodeClass.getSimpleName());
    }

    private void verifyWrittenValue(final NormalizedNode value) {
        checkArgument(nodeClass.isInstance(value), "Expected an instance of %s, have %s", nodeClass, value);
    }
}
