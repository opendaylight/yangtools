/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class UnorderedMapMixinContextNode extends AbstractMixinContextNode<NodeIdentifier> {
    private final ListItemContextNode innerNode;

    UnorderedMapMixinContextNode(final ListSchemaNode list) {
        super(NodeIdentifier.create(list.getQName()), list);
        this.innerNode = new ListItemContextNode(new NodeIdentifierWithPredicates(list.getQName()), list);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        if (child.getNodeType().equals(getIdentifier().getNodeType())) {
            return innerNode;
        }
        return null;
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        if (getIdentifier().getNodeType().equals(child)) {
            return innerNode;
        }
        return null;
    }
}
