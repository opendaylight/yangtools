/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateTip;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeTip;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

abstract class AbstractDataTreeTip implements DataTreeTip {
    /**
     * Return the current root node of this tip.
     *
     * @return Current tip root node, may not be null.
     */
    protected abstract @NonNull TreeNode getTipRoot();

    abstract @NonNull YangInstanceIdentifier getRootPath();

    @Override
    public final void validate(final DataTreeModification modification) throws DataValidationFailedException {
        final InMemoryDataTreeModification m = checkedCast(modification);
        checkArgument(m.isSealed(), "Attempted to verify unsealed modification %s", m);

        m.getStrategy().checkApplicable(new ModificationPath(getRootPath()), m.getRootModification(), getTipRoot(),
            m.getVersion());
    }

    @Override
    public final DataTreeCandidateTip prepare(final DataTreeModification modification) {
        final InMemoryDataTreeModification m = checkedCast(modification);
        checkArgument(m.isSealed(), "Attempted to prepare unsealed modification %s", m);

        final ModifiedNode root = m.getRootModification();

        final TreeNode currentRoot = getTipRoot();
        if (root.getOperation() == LogicalOperation.NONE) {
            return new NoopDataTreeCandidate(YangInstanceIdentifier.of(), root, currentRoot);
        }

        final var newRoot = m.getStrategy().apply(m.getRootModification(), currentRoot, m.getVersion())
            .orElseThrow(() -> new IllegalStateException(
                "Apply strategy failed to produce root node for modification " + modification));
        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), root, currentRoot, newRoot);
    }

    private static InMemoryDataTreeModification checkedCast(final DataTreeModification mod) {
        checkArgument(mod instanceof InMemoryDataTreeModification, "Invalid modification class %s", mod.getClass());
        return (InMemoryDataTreeModification)mod;
    }
}
