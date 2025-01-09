/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateTip;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeTip;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

@NonNullByDefault
abstract class AbstractDataTreeTip implements DataTreeTip {
    /**
     * Return the current root node of this tip.
     *
     * @return Current tip root node, may not be null.
     */
    abstract TreeNode getTipRoot();

    abstract YangInstanceIdentifier getRootPath();

    @Override
    public final void validate(final DataTreeModification modification) throws DataValidationFailedException {
        accessMod(modification).validate(getRootPath(), getTipRoot());
    }

    @Override
    public final DataTreeCandidateTip prepare(final DataTreeModification modification) {
        return accessMod(modification).prepare(getRootPath(), getTipRoot());
    }

    private static InMemoryDataTreeModification accessMod(final DataTreeModification mod) {
        if (mod instanceof InMemoryDataTreeModification inMemoryMod) {
            return inMemoryMod;
        }
        throw new IllegalArgumentException("Invalid modification " + mod.getClass());
    }
}
