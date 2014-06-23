/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;


import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

import com.google.common.base.Preconditions;

abstract class AbstractDataTreeCandidate implements DataTreeCandidate {
    private final InstanceIdentifier rootPath;

    protected AbstractDataTreeCandidate(final InstanceIdentifier rootPath) {
        this.rootPath = Preconditions.checkNotNull(rootPath);
    }

    @Override
    public final InstanceIdentifier getRootPath() {
        return rootPath;
    }

}
