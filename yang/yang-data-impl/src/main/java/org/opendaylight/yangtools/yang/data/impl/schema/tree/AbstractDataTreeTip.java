/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateTip;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeTip;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

abstract class AbstractDataTreeTip implements DataTreeTip {
    /**
     * Return the current root node of this tip.
     *
     * @return Current tip root node, may not be null.
     */
    @Nonnull protected abstract TreeNode getTipRoot();

    @Override
    public final void validate(final DataTreeModification modification) throws DataValidationFailedException {
        final InMemoryDataTreeModification m = castModification(modification);
        checkArgument(m.isSealed(), "Attempted to verify unsealed modification %s", m);

        m.getStrategy().checkApplicable(YangInstanceIdentifier.EMPTY, m.getRootModification(),
            Optional.of(getTipRoot()), m.getVersion());
    }

    @Override
    public final DataTreeCandidateTip prepare(final DataTreeModification modification) {
        final InMemoryDataTreeModification m = castModification(modification);
        checkArgument(m.isSealed(), "Attempted to prepare unsealed modification %s", m);

        final ModifiedNode root = m.getRootModification();

        final TreeNode currentRoot = getTipRoot();
        if (root.getOperation() == LogicalOperation.NONE) {
            return new NoopDataTreeCandidate(YangInstanceIdentifier.EMPTY, root, currentRoot);
        }

        final Optional<TreeNode> newRoot = m.getStrategy().apply(m.getRootModification(),
            Optional.of(currentRoot), m.getVersion());
        checkState(newRoot.isPresent(), "Apply strategy failed to produce root node for modification %s",
            modification);
        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.EMPTY, root, currentRoot, newRoot.get());
    }

    private static InMemoryDataTreeModification castModification(final DataTreeModification modification) {
        checkArgument(modification instanceof InMemoryDataTreeModification,
            "Invalid modification class %s", modification.getClass());
        return (InMemoryDataTreeModification) modification;
    }
}
