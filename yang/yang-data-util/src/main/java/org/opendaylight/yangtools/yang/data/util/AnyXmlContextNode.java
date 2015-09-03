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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;

class AnyXmlContextNode extends DataSchemaContextNode<NodeIdentifier> {

    protected AnyXmlContextNode(final AnyXmlSchemaNode schema) {
        super(NodeIdentifier.create(schema.getQName()), schema);
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
        return false;
    }

}
