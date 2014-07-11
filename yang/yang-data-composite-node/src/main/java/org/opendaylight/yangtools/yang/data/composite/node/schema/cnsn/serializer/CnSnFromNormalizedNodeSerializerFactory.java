/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.serializer;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializerFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class CnSnFromNormalizedNodeSerializerFactory implements FromNormalizedNodeSerializerFactory<Node<?>> {
    private final ContainerNodeCnSnSerializer containerSerializer;
    private final ChoiceNodeCnSnSerializer choiceSerializer;
    private final AugmentationNodeCnSnSerializer augmentSerializer;
    private final LeafNodeCnSnSerializer leafNodeSerializer;
    private final LeafSetNodeCnSnSerializer leafSetSerializer;
    private final MapNodeCnSnSerializer mapNodeSerializer;
    private final LeafSetEntryNodeCnSnSerializer leafSetEntryNodeSerializer;
    private final MapEntryNodeCnSnSerializer mapEntryNodeSerializer;
    private final AnyXmlNodeCnSnSerializer anyXmlNodeSerializer;

    private CnSnFromNormalizedNodeSerializerFactory() {
        final NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Node<?>> dispatcher = new NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Node<?>>(
                this) {

        };

        containerSerializer = new ContainerNodeCnSnSerializer(dispatcher);
        choiceSerializer = new ChoiceNodeCnSnSerializer(dispatcher);
        augmentSerializer = new AugmentationNodeCnSnSerializer(dispatcher);
        leafNodeSerializer = new LeafNodeCnSnSerializer();
        anyXmlNodeSerializer = new AnyXmlNodeCnSnSerializer();

        leafSetEntryNodeSerializer = new LeafSetEntryNodeCnSnSerializer();
        leafSetSerializer = new LeafSetNodeCnSnSerializer(leafSetEntryNodeSerializer);

        mapEntryNodeSerializer = new MapEntryNodeCnSnSerializer(dispatcher);
        mapNodeSerializer = new MapNodeCnSnSerializer(mapEntryNodeSerializer);
    }


    public static CnSnFromNormalizedNodeSerializerFactory getInstance() {
        return new CnSnFromNormalizedNodeSerializerFactory();
}

    @Override
    public FromNormalizedNodeSerializer<Node<?>, ContainerNode, ContainerSchemaNode> getContainerNodeSerializer() {
        return containerSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, LeafNode<?>, LeafSchemaNode> getLeafNodeSerializer() {
        return leafNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeSerializer() {
        return leafSetSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, MapNode, ListSchemaNode> getMapNodeSerializer() {
        return mapNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> getChoiceNodeSerializer() {
        return choiceSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, AugmentationNode, AugmentationSchema> getAugmentationNodeSerializer() {
        return augmentSerializer;
    }


    @Override
    public FromNormalizedNodeSerializer<Node<?>, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeSerializer() {
        return leafSetEntryNodeSerializer;
    }


    @Override
    public FromNormalizedNodeSerializer<Node<?>, MapEntryNode, ListSchemaNode> getMapEntryNodeSerializer() {
        return mapEntryNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Node<?>, AnyXmlNode, AnyXmlSchemaNode> getAnyXmlNodeSerializer() {
        return anyXmlNodeSerializer;
    }
}
