/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import java.util.Optional;
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
        final var m = accessMod(modification, "validate");
        m.getStrategy().checkApplicable(new ModificationPath(getRootPath()), m.getRootModification(),
            Optional.of(getTipRoot()), m.getVersion());
    }

    @Override
    public final DataTreeCandidateTip prepare(final DataTreeModification modification) {
        final var m = accessMod(modification, "prepare");
        final var root = m.getRootModification();

        final var currentRoot = getTipRoot();
        if (root.getOperation() == LogicalOperation.NONE) {
            return new NoopDataTreeCandidate(YangInstanceIdentifier.of(), root, currentRoot);
        }

        final var newRoot = m.getStrategy().apply(m.getRootModification(), Optional.of(currentRoot), m.getVersion())
            .orElseThrow(() -> new IllegalStateException("Apply strategy failed to produce root node for modification "
                + modification));
        return new InMemoryDataTreeCandidate(YangInstanceIdentifier.of(), root, currentRoot, newRoot);
    }

    private static @NonNull InMemoryDataTreeModification accessMod(final DataTreeModification mod, final String op) {
        if (mod instanceof InMemoryDataTreeModification inMemoryMod) {
            if (inMemoryMod.isSealed()) {
                return inMemoryMod;
            }
            throw new IllegalArgumentException("Attempted to " + op + " unsealed modification " + inMemoryMod);
        }
        throw new IllegalArgumentException("Invalid modification " + mod.getClass());
    }
}
