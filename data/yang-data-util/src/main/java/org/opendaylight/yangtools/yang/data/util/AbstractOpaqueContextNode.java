/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractOpaqueContextNode<S extends DataSchemaNode> extends AbstractLeafContextNode<NodeIdentifier, S> {
    AbstractOpaqueContextNode(final S schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
    }

    @Override
    public final boolean isLeaf() {
        return false;
    }
}
