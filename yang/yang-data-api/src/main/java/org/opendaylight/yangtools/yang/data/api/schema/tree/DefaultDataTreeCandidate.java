/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Default utility implementation of the {@link DataTreeCandidate} contract.
 */
final class DefaultDataTreeCandidate implements DataTreeCandidate {
    private final YangInstanceIdentifier rootPath;
    private final DataTreeCandidateNode rootNode;

    DefaultDataTreeCandidate(final YangInstanceIdentifier rootPath, final DataTreeCandidateNode rootNode) {
        this.rootPath = requireNonNull(rootPath);
        this.rootNode = requireNonNull(rootNode);
    }

    @Override
    public DataTreeCandidateNode getRootNode() {
        return rootNode;
    }

    @Override
    public YangInstanceIdentifier getRootPath() {
        return rootPath;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("rootPath", getRootPath()).add("rootNode", getRootNode())
            .toString();
    }
}
