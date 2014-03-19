/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class DomNodeSerializerDispatcher extends NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Element> {

    private static DomNodeSerializerDispatcher instance;
    private static Document document;

    private final FromNormalizedNodeSerializer<Element, ContainerNode, ContainerSchemaNode> containerSerializer;
    private final FromNormalizedNodeSerializer<Element, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> choiceSerializer;
    private final FromNormalizedNodeSerializer<Element, AugmentationNode, AugmentationSchema> augmentSerializer;
    private final FromNormalizedNodeSerializer<Element, LeafNode<?>, LeafSchemaNode> leafNodeSerializer;
    private final FromNormalizedNodeSerializer<Element, LeafSetNode<?>, LeafListSchemaNode> leafSetSerializer;
    private final FromNormalizedNodeSerializer<Element, MapNode, ListSchemaNode> mapNodeSerializer;

    DomNodeSerializerDispatcher(Document doc, XmlCodecProvider codecProvider) {
        containerSerializer = new ContainerNodeDomSerializer(doc, codecProvider);
        choiceSerializer = new ChoiceNodeDomSerializer(doc, codecProvider);
        augmentSerializer = new AugmentationNodeDomSerializer(doc, codecProvider);
        leafNodeSerializer = new LeafNodeDomSerializer(doc, codecProvider);
        leafSetSerializer = new LeafSetNodeDomSerializer(new LeafSetEntryNodeDomSerializer(doc, codecProvider));
        mapNodeSerializer = new MapNodeDomSerializer(new MapEntryNodeDomSerializer(doc, codecProvider));
    }

    static DomNodeSerializerDispatcher getInstance(Document doc, XmlCodecProvider codecProvider) {
        // Recreate the singleton instance if the document parameter doc is a new instance
        // Dom does not allow to add elements to one document from another

        // TODO analyze race conditions
        if (instance == null || document != doc) {
            document = doc;
            instance = new DomNodeSerializerDispatcher(doc, codecProvider);
        }
        return instance;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, ContainerNode, ContainerSchemaNode> getContainerNodeSerializer() {
        return containerSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, LeafNode<?>, LeafSchemaNode> getLeafNodeSerializer() {
        return leafNodeSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeSerializer() {
        return leafSetSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, MapNode, ListSchemaNode> getMapNodeSerializer() {
        return mapNodeSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> getChoiceNodeSerializer() {
        return choiceSerializer;
    }

    @Override
    protected FromNormalizedNodeSerializer<Element, AugmentationNode, AugmentationSchema> getAugmentationNodeSerializer() {
        return augmentSerializer;
    }
}
