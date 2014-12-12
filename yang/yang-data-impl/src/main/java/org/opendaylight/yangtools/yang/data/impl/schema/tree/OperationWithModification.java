/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

final class OperationWithModification {

    private final ModifiedNode modification;

    private final ModificationApplyOperation applyOperation;

    private OperationWithModification(final ModificationApplyOperation op, final ModifiedNode mod) {
        this.modification = mod;
        this.applyOperation = op;
    }

    void write(final NormalizedNode<?, ?> value) {
        modification.write(value);
        applyOperation.verifyStructure(modification);
    }

    private void mergeImpl(final NormalizedNode<?,?> data) {
        if (data instanceof NormalizedNodeContainer<?,?,?>) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            NormalizedNodeContainer<?,?,NormalizedNode<PathArgument, ?>> dataContainer = (NormalizedNodeContainer) data;
            for (NormalizedNode<PathArgument, ?> child : dataContainer.getValue()) {
                PathArgument childId = child.getIdentifier();
                forChild(childId).mergeImpl(child);
            }
        }

        modification.merge(data);
    }

    void merge(final NormalizedNode<?, ?> data) {
        mergeImpl(data);
        applyOperation.verifyStructure(modification);
    }

    void delete() {
        modification.delete();
    }

    public ModifiedNode getModification() {
        return modification;
    }

    public ModificationApplyOperation getApplyOperation() {
        return applyOperation;
    }

    public Optional<TreeNode> apply(final Optional<TreeNode> data, final Version version) {
        return applyOperation.apply(modification, data, version);
    }

    public static OperationWithModification from(final ModificationApplyOperation operation,
            final ModifiedNode modification) {
        return new OperationWithModification(operation, modification);
    }

    private OperationWithModification forChild(final PathArgument childId) {
        ModificationApplyOperation childOp = applyOperation.getChild(childId).get();

        final boolean isOrdered;
        if (childOp instanceof SchemaAwareApplyOperation) {
            isOrdered = ((SchemaAwareApplyOperation) childOp).isOrdered();
        } else {
            isOrdered = true;
        }

        ModifiedNode childMod = modification.modifyChild(childId, isOrdered);

        return from(childOp,childMod);
    }
}
