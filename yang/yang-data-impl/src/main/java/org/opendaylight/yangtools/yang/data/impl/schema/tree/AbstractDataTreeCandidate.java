/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateTip;

@NonNullByDefault
abstract class AbstractDataTreeCandidate extends AbstractDataTreeTip implements DataTreeCandidateTip {
    private final YangInstanceIdentifier rootPath;

    protected AbstractDataTreeCandidate(final YangInstanceIdentifier rootPath) {
        this.rootPath = Preconditions.checkNotNull(rootPath);
    }

    @Override
    public final YangInstanceIdentifier getRootPath() {
        return rootPath;
    }
}
