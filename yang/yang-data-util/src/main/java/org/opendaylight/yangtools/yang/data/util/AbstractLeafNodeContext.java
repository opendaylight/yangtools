/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractLeafNodeContext<T extends PathArgument> extends DataSchemaContextNode<T> {

    protected AbstractLeafNodeContext(final T identifier, final DataSchemaNode potential) {
        super(identifier, potential);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        return null;
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

}