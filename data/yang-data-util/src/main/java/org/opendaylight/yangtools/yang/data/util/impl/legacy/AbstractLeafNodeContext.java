/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractLeafNodeContext<T extends PathArgument, S extends DataSchemaNode>
        extends AbstractLeafContextNode<T, S> {
    AbstractLeafNodeContext(final T identifier, final S potential) {
        super(identifier, potential);
    }

    @Override
    public final boolean isLeaf() {
        return true;
    }
}
