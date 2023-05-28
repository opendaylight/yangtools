/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

final class InMemoryDataTreeCandidate extends AbstractDataTreeCandidate {
    private static final class RootNode extends AbstractModifiedNodeBasedCandidateNode {
        RootNode(final ModifiedNode mod, final TreeNode oldMeta, final TreeNode newMeta) {
            super(mod, oldMeta, newMeta);
        }

        @Override
        public PathArgument name() {
            throw new IllegalStateException("Attempted to get identifier of the root node");
        }
    }

    private final RootNode root;

    InMemoryDataTreeCandidate(final YangInstanceIdentifier rootPath, final ModifiedNode modificationRoot,
            final TreeNode beforeRoot, final TreeNode afterRoot) {
        super(rootPath);
        root = new RootNode(modificationRoot, beforeRoot, afterRoot);
    }

    @Override
    protected TreeNode getTipRoot() {
        return root.getNewMeta();
    }

    TreeNode getBeforeRoot() {
        return root.getOldMeta();
    }

    @Override
    public DataTreeCandidateNode getRootNode() {
        return root;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("rootPath", getRootPath())
                .add("rootNode", getRootNode()).toString();
    }
}
