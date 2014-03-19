/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

final class DomNodeSerializerDispatcher extends NodeSerializerDispatcher.BaseNodeSerializerDispatcher<Element> {
    private static final Cache<Document, DomNodeSerializerDispatcher> dispatcherCache =
            CacheBuilder.newBuilder().weakKeys().build();

    private final FromNormalizedNodeSerializer<Element, ContainerNode, ContainerSchemaNode> containerSerializer;
    private final FromNormalizedNodeSerializer<Element, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> choiceSerializer;
    private final FromNormalizedNodeSerializer<Element, AugmentationNode, AugmentationSchema> augmentSerializer;
    private final FromNormalizedNodeSerializer<Element, LeafNode<?>, LeafSchemaNode> leafNodeSerializer;
    private final FromNormalizedNodeSerializer<Element, LeafSetNode<?>, LeafListSchemaNode> leafSetSerializer;
    private final FromNormalizedNodeSerializer<Element, MapNode, ListSchemaNode> mapNodeSerializer;

    DomNodeSerializerDispatcher(Document doc, XmlCodecProvider codecProvider) {
        containerSerializer = new ContainerNodeDomSerializer(doc, this);
        choiceSerializer = new ChoiceNodeDomSerializer(this);
        augmentSerializer = new AugmentationNodeDomSerializer(this);
        leafNodeSerializer = new LeafNodeDomSerializer(doc, codecProvider);
        leafSetSerializer = new LeafSetNodeDomSerializer(new LeafSetEntryNodeDomSerializer(doc, codecProvider));
        mapNodeSerializer = new MapNodeDomSerializer(new MapEntryNodeDomSerializer(doc, this));
    }

    // FIXME: Callers should really grow a proper (per-document) lifecycle and not rely on this
    @Deprecated
    static DomNodeSerializerDispatcher getInstance(final Document doc, final XmlCodecProvider codecProvider) {
        // DOM does not allow to add elements to one document from another, so maintain a cache
        // on a per-document case.
        try {
            return dispatcherCache.get(doc, new Callable<DomNodeSerializerDispatcher>() {
                @Override
                public DomNodeSerializerDispatcher call() {
                    return new DomNodeSerializerDispatcher(doc, codecProvider);
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to instantiated a dispatcher", e);
        }
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
