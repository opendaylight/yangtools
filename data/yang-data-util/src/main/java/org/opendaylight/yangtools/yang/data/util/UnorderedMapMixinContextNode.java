/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class UnorderedMapMixinContextNode extends AbstractListLikeContextNode<NodeIdentifier> {
    private final ListItemContextNode innerNode;

    UnorderedMapMixinContextNode(final ListSchemaNode list) {
        super(NodeIdentifier.create(list.getQName()), list);
        innerNode = new ListItemContextNode(list);
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final PathArgument child) {
        // FIXME: validate PathArgument type
        return innerNodeIfMatch(child.getNodeType());
    }

    @Override
    public final DataSchemaContextNode<?> getChild(final QName child) {
        return innerNodeIfMatch(child);
    }

    // FIXME: dead ringers in other AbstractMixinContextNode subclasses
    private @Nullable DataSchemaContextNode<?> innerNodeIfMatch(final QName qname) {
        // FIXME: 10.0.0: requireNonNull(qname)
        return getIdentifier().getNodeType().equals(qname) ? innerNode : null;
    }
}
