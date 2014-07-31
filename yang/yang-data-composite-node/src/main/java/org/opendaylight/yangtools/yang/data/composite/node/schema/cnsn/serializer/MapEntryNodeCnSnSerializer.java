/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.serializer;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.MapEntryNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.data.impl.util.CompositeNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class MapEntryNodeCnSnSerializer extends MapEntryNodeBaseSerializer<Node<?>> {

    private final NodeSerializerDispatcher<Node<?>> dispatcher;

    MapEntryNodeCnSnSerializer(final NodeSerializerDispatcher<Node<?>> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    @Override
    public List<Node<?>> serialize(ListSchemaNode schema, MapEntryNode node) {
        CompositeNodeBuilder<ImmutableCompositeNode> compNodeBuilder = ImmutableCompositeNode.builder();
        compNodeBuilder.setQName(node.getNodeType());
        compNodeBuilder.addAll(super.serialize(schema, node));
        return Collections.<Node<?>> singletonList(compNodeBuilder.toInstance());        
    }

    @Override
    protected NodeSerializerDispatcher<Node<?>> getNodeDispatcher() {
        return dispatcher;
    }
}
