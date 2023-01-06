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

class UnorderedLeafListMixinContextNode extends AbstractListLikeContextNode<NodeIdentifier> {
    private final LeafListEntryContextNode innerOp;

    UnorderedLeafListMixinContextNode(final LeafListSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
        innerOp = new LeafListEntryContextNode(schema);
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final PathArgument child) {
        // FIXME: 10.0.0: reject null and invalid
        return child instanceof NodeWithValue ? innerOp : null;
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final QName child) {
        // FIXME: requireNonNull, common code with UnkeyedListMixinNode
        return getIdentifier().getNodeType().equals(child) ? innerOp : null;
    }

    @Override
    public final boolean isLeaf() {
        return true;
    }

    @Override
    public final boolean isKeyedEntry() {
        return true;
    }
}
