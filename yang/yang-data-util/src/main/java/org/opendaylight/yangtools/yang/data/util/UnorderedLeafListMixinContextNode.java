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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

class UnorderedLeafListMixinContextNode extends AbstractMixinContextNode<NodeIdentifier> {

    private final DataSchemaContextNode<?> innerOp;

    public UnorderedLeafListMixinContextNode(final LeafListSchemaNode potential) {
        super(NodeIdentifier.create(potential.getQName()), potential);
        innerOp = new LeafListEntryContextNode(potential);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        if (child instanceof NodeWithValue) {
            return innerOp;
        }
        return null;
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        if (getIdentifier().getNodeType().equals(child)) {
            return innerOp;
        }
        return null;
    }
}
