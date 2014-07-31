/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.serializer;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.NodeFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.LeafSetEntryNodeBaseSerializer;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class LeafSetEntryNodeCnSnSerializer extends
        LeafSetEntryNodeBaseSerializer<Node<?>> {

    @Override
    protected Node<?> serializeLeaf(LeafListSchemaNode schema, LeafSetEntryNode<?> node) {
        return NodeFactory.createMutableSimpleNode(
				node.getNodeType(), null, node.getValue(), null, null);
    }
}
