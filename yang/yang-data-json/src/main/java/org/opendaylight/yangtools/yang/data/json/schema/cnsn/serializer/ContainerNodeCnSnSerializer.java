/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.serializer;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.NodeFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.ContainerNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public class ContainerNodeCnSnSerializer extends
		ContainerNodeBaseSerializer<Node<?>> {

	@Override
	public List<Node<?>> serialize(ContainerSchemaNode schema,
			ContainerNode containerNode) {

		MutableCompositeNode mutCompNode = NodeFactory.createMutableCompositeNode(
				containerNode.getNodeType(), null, null, null, null);
		
		for (Node<?> element : super.serialize(schema, containerNode)) {
			if(element instanceof MutableNode<?>) {
				((MutableNode<?>) element).setParent(mutCompNode);
			}
			mutCompNode.getValue().add(element);
		}
		
		List<Node<?>> lst = new ArrayList<>();
		lst.add(mutCompNode);
		return lst;
	}

	@Override
	protected NodeSerializerDispatcher<Node<?>> getNodeDispatcher() {
		return CnSnNodeSerializerDispatcher.getInstance();
	}

}
