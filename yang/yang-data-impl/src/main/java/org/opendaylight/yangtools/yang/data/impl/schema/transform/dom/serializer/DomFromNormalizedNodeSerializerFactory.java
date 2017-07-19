/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializerFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public final class DomFromNormalizedNodeSerializerFactory implements FromNormalizedNodeSerializerFactory<Element> {
    private final ContainerNodeDomSerializer containerSerializer;
    private final ChoiceNodeDomSerializer choiceSerializer;
    private final AugmentationNodeDomSerializer augmentSerializer;
    private final LeafNodeDomSerializer leafNodeSerializer;
    private final LeafSetNodeDomSerializer leafSetSerializer;
    private final MapNodeDomSerializer mapNodeSerializer;
    private final UnkeyedListNodeDomSerializer unkeyedListNodeSerializer;
    private final LeafSetEntryNodeDomSerializer leafSetEntryNodeSerializer;
    private final MapEntryNodeDomSerializer mapEntryNodeSerializer;
    private final UnkeyedListEntryNodeDomSerializer unkeyedListEntryNodeSerializer;

    private DomFromNormalizedNodeSerializerFactory(final Document doc, final XmlCodecProvider codecProvider) {
        final NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Element> dispatcher = new NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Element>(this) {

        };

        containerSerializer = new ContainerNodeDomSerializer(doc, dispatcher);
        choiceSerializer = new ChoiceNodeDomSerializer(dispatcher);
        augmentSerializer = new AugmentationNodeDomSerializer(dispatcher);
        leafNodeSerializer = new LeafNodeDomSerializer(doc, codecProvider);

        leafSetEntryNodeSerializer = new LeafSetEntryNodeDomSerializer(doc, codecProvider);
        leafSetSerializer = new LeafSetNodeDomSerializer(leafSetEntryNodeSerializer);

        mapEntryNodeSerializer = new MapEntryNodeDomSerializer(doc, dispatcher);
        mapNodeSerializer = new MapNodeDomSerializer(mapEntryNodeSerializer);

        unkeyedListEntryNodeSerializer = new UnkeyedListEntryNodeDomSerializer(doc, dispatcher);
        unkeyedListNodeSerializer = new UnkeyedListNodeDomSerializer(unkeyedListEntryNodeSerializer);
    }

    public static DomFromNormalizedNodeSerializerFactory getInstance(final Document doc, final XmlCodecProvider codecProvider) {
        return new DomFromNormalizedNodeSerializerFactory(doc, codecProvider);
    }

    @Override
    public FromNormalizedNodeSerializer<Element, AugmentationNode, AugmentationSchema> getAugmentationNodeSerializer() {
        return augmentSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, ChoiceNode, ChoiceSchemaNode> getChoiceNodeSerializer() {
        return choiceSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, ContainerNode, ContainerSchemaNode> getContainerNodeSerializer() {
        return containerSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, LeafNode<?>, LeafSchemaNode> getLeafNodeSerializer() {
        return leafNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeSerializer() {
        return leafSetEntryNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeSerializer() {
        return leafSetSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, MapEntryNode, ListSchemaNode> getMapEntryNodeSerializer() {
        return mapEntryNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, MapNode, ListSchemaNode> getMapNodeSerializer() {
        return mapNodeSerializer;
    }

    @Override
    public FromNormalizedNodeSerializer<Element, UnkeyedListNode, ListSchemaNode> getUnkeyedListNodeSerializer() {
        return unkeyedListNodeSerializer;
    }

        @Override
        public FromNormalizedNodeSerializer<Element, AnyXmlNode, AnyXmlSchemaNode> getAnyXmlNodeSerializer() {
            throw new UnsupportedOperationException();
        }

}
