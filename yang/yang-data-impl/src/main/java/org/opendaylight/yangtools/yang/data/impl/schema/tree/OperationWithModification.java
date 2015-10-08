/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

final class OperationWithModification {
    private static final Function<TreeNode, NormalizedNode<?, ?>> READ_DATA = new Function<TreeNode, NormalizedNode<?, ?>>() {
        @Override
        public NormalizedNode<?, ?> apply(final TreeNode input) {
            return input.getData();
        }
    };

    private final ModifiedNode modification;
    private final ModificationApplyOperation applyOperation;

    private OperationWithModification(final ModificationApplyOperation op, final ModifiedNode mod) {
        this.modification = Preconditions.checkNotNull(mod);
        this.applyOperation = Preconditions.checkNotNull(op);
    }

    void write(final NormalizedNode<?, ?> value) {
        modification.write(value);
        /**
         * Fast validation of structure, full validation on written data will be run during seal.
         */
        applyOperation.verifyStructure(value, false);
    }

    private void recursiveMerge(final NormalizedNode<?,?> data, final Version version) {
        if (data instanceof NormalizedNodeContainer) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final
            NormalizedNodeContainer<?,?,NormalizedNode<PathArgument, ?>> dataContainer = (NormalizedNodeContainer) data;

            /*
             * if there was write before on this node and it is of NormalizedNodeContainer type
             * merge would overwrite our changes. So we create write modifications from data children to
             * retain children created by past write operation.
             * These writes will then be pushed down in the tree while there are merge modifications on these children
             */
            if (modification.getOperation() == LogicalOperation.WRITE) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final
                NormalizedNodeContainer<?,?,NormalizedNode<PathArgument, ?>> odlDataContainer =
                        (NormalizedNodeContainer) modification.getWrittenValue();
                for (final NormalizedNode<PathArgument, ?> child : odlDataContainer.getValue()) {
                    final PathArgument childId = child.getIdentifier();
                    forChild(childId, version).write(child);
                }
            }
            for (final NormalizedNode<PathArgument, ?> child : dataContainer.getValue()) {
                final PathArgument childId = child.getIdentifier();
                forChild(childId, version).recursiveMerge(child, version);
            }
        }

        modification.merge(data);
    }

    void merge(final NormalizedNode<?, ?> data, final Version version) {
        /*
         * A merge operation will end up overwriting parts of the tree, retaining others. We want to
         * make sure we do not validate the complete resulting structure, but rather just what was
         * written. In order to do that, we first pretend the data was written, run verification and
         * then perform the merge -- with the explicit assumption that adding the newly-validated
         * data with the previously-validated data will not result in invalid data.
         *
         * FIXME: Should be this moved to recursive merge and run for each node?
         */
        applyOperation.verifyStructure(data, false);
        recursiveMerge(data, version);
    }

    void delete() {
        modification.delete();
    }

    /**
     * Read a particular child. If the child has been modified and does not have a stable
     * view, one will we instantiated with specified version.
     *
     * @param child
     * @param version
     * @return
     */
    Optional<NormalizedNode<?, ?>> read(final PathArgument child, final Version version) {
        final Optional<ModifiedNode> maybeChild = modification.getChild(child);
        if (maybeChild.isPresent()) {
            final ModifiedNode childNode = maybeChild.get();

            Optional<TreeNode> snapshot = childNode.getSnapshot();
            if (snapshot == null) {
                // Snapshot is not present, force instantiation
                snapshot = applyOperation.getChild(child).get().apply(childNode, childNode.getOriginal(), version);
            }

            return snapshot.transform(READ_DATA);
        }

        Optional<TreeNode> snapshot = modification.getSnapshot();
        if (snapshot == null) {
            snapshot = apply(modification.getOriginal(), version);
        }

        if (snapshot.isPresent()) {
            return snapshot.get().getChild(child).transform(READ_DATA);
        }

        return Optional.absent();
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

    private OperationWithModification forChild(final PathArgument childId, final Version version) {
        final Optional<ModificationApplyOperation> maybeChildOp = applyOperation.getChild(childId);
        Preconditions.checkArgument(maybeChildOp.isPresent(),
            "Attempted to apply operation to non-existent child %s", childId);

        final ModificationApplyOperation childOp = maybeChildOp.get();
        final ModifiedNode childMod = modification.modifyChild(childId, childOp.getChildPolicy(), version);

        return from(childOp, childMod);
    }
}
