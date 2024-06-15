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

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Unmodified;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

/**
 * Internal utility class for an empty candidate. We instantiate this class
 * for empty modifications, saving memory and processing speed. Instances
 * of this class are explicitly recognized and processing of them is skipped.
 */
final class NoopDataTreeCandidate extends AbstractDataTreeCandidate {
    private final @NonNull TreeNode afterRoot;

    protected NoopDataTreeCandidate(final YangInstanceIdentifier rootPath, final ModifiedNode modificationRoot,
            final TreeNode afterRoot) {
        super(rootPath);
        checkArgument(modificationRoot.getOperation() == LogicalOperation.NONE);
        this.afterRoot = requireNonNull(afterRoot);
    }

    @Override
    public Unmodified getRootNode() {
        return () -> {
            throw new UnsupportedOperationException("Attempted to read data of the no-operation change");
        };
    }

    @Override
    protected TreeNode getTipRoot() {
        return afterRoot;
    }
}
