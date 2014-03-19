/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.json.serializer;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class NormNodeSerializerDispatcher extends NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Node<?>> {

    private static NormNodeSerializerDispatcher instance;

    private final FromNormalizedNodeSerializer<Node<?>, ContainerNode, ContainerSchemaNode> containerSerializer;
    private final FromNormalizedNodeSerializer<Node<?>, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> choiceSerializer;
    private final FromNormalizedNodeSerializer<Node<?>, AugmentationNode, AugmentationSchema> augmentSerializer;
    private final FromNormalizedNodeSerializer<Node<?>, LeafNode<?>, LeafSchemaNode> leafNodeSerializer;
    private final FromNormalizedNodeSerializer<Node<?>, LeafSetNode<?>, LeafListSchemaNode> leafSetSerializer;
    private final FromNormalizedNodeSerializer<Node<?>, MapNode, ListSchemaNode> mapNodeSerializer;

    NormNodeSerializerDispatcher() {
        containerSerializer = new ContainerNodeNormSerializer();
        choiceSerializer = new ChoiceNodeNormSerializer();
        augmentSerializer = new AugmentationNodeNormSerializer();
        leafNodeSerializer = new LeafNodeNormSerializer();
        leafSetSerializer = new LeafSetNodeNormSerializer();
        mapNodeSerializer = new MapNodeNormSerializer();
    }

    static NormNodeSerializerDispatcher getInstance() {
        if (instance == null) {
            instance = new NormNodeSerializerDispatcher();
        }
        return instance;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, ContainerNode, ContainerSchemaNode> getContainerNodeSerializer() {
        return containerSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, LeafNode<?>, LeafSchemaNode> getLeafNodeSerializer() {
        return leafNodeSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeSerializer() {
        return leafSetSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, MapNode, ListSchemaNode> getMapNodeSerializer() {
        return mapNodeSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> getChoiceNodeSerializer() {
        return choiceSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Node<?>, AugmentationNode, AugmentationSchema> getAugmentationNodeSerializer() {
        return augmentSerializer;
    }
}
