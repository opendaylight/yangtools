/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

/**
 * Default utility implementation of the {@link DataTreeCandidate} contract.
 * @deprecated Use {@link DataTreeCandidates#newDataTreeCandidate} instead.
 */
@Deprecated
public final class DefaultDataTreeCandidate implements DataTreeCandidate {
    private final YangInstanceIdentifier rootPath;
    private final DataTreeCandidateNode rootNode;

    public DefaultDataTreeCandidate(final YangInstanceIdentifier rootPath, final DataTreeCandidateNode rootNode) {
        this.rootPath = Preconditions.checkNotNull(rootPath);
        this.rootNode = Preconditions.checkNotNull(rootNode);
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
        return MoreObjects.toStringHelper(this).add("rootPath", getRootPath()).add("rootNode", getRootNode()).toString();
    }
}
