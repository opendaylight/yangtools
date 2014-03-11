/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

abstract class AbstractDispatcherParser<I extends InstanceIdentifier.PathArgument, N extends DataContainerNode<I>, S>
        implements DomParser<I, N, S> {

    protected abstract DataContainerNodeBuilder<I, N> getBuilder(S schema);

    @Override
    public N fromDom(List<Element> xml, S schema, XmlCodecProvider codecProvider) {

        checkAtLeastOneNode(schema, xml);

        DataContainerNodeBuilder<I, N> containerBuilder = getBuilder(schema);

        // Map child nodes to QName
        LinkedListMultimap<QName, Element> mappedChildElements = mapChildElements(xml);

        // Map child nodes from Augments
        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapChildElementsFromAugments(schema);
        LinkedListMultimap<AugmentationSchema, Element> augmentsToElements = LinkedListMultimap.create();

        // Map child nodes from choices
        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChildElementsFromChoices(schema);
        LinkedListMultimap<ChoiceNode, Element> choicesToElements = LinkedListMultimap.create();

        // process Child nodes
        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = getSchemaForChild(schema, childPartialQName);
            List<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            // Augment
            if (isMarkedAs(mappedAugmentChildNodes, childSchema.getQName())) {
                AugmentationSchema augmentationSchema = mappedAugmentChildNodes.get(childSchema.getQName());
                augmentsToElements.putAll(augmentationSchema, childrenForQName);
                // Choices
            } else if (isMarkedAs(mappedChoiceChildNodes, childSchema.getQName())) {
                ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                choicesToElements.putAll(choiceSchema, childrenForQName);
                // Regular child nodes
            } else {
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = NodeDispatcher
                        .dispatchChildElement(childSchema, childrenForQName, codecProvider);
                containerBuilder.withChild(builtChildNode);
            }
        }

        // Augments and choices are processed at the end, when all child nodes
        // are present

        // TODO ordering is not preserved for augments and choices

        for (ChoiceNode choiceSchema : choicesToElements.keySet()) {
            containerBuilder.withChild(NodeDispatcher.dispatchChildElement(choiceSchema,
                    choicesToElements.get(choiceSchema), codecProvider));
        }

        for (AugmentationSchema augmentSchema : augmentsToElements.keySet()) {
            Set<DataSchemaNode> realChildSchemas = getRealSchemasForAugment(schema, augmentSchema);
            AugmentationSchema augProxy = new AugmentationSchemaProxy(augmentSchema, realChildSchemas);
            containerBuilder.withChild(NodeDispatcher.dispatchChildElement(augProxy,
                    augmentsToElements.get(augmentSchema), codecProvider));
        }

        return containerBuilder.build();
    }

    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema augmentSchema);

    private boolean isMarkedAs(Map<QName, ?> mappedAugmentChildNodes, QName qName) {
        return mappedAugmentChildNodes.containsKey(qName);
    }

    protected abstract LinkedListMultimap<QName, Element> mapChildElements(List<Element> xml);

    protected void checkOnlyOneNode(S schema, Collection<Element> childNodes) {
        Preconditions.checkArgument(childNodes.size() == 1,
                "Node detected multiple times, should be 1, identified by: %s, found: %s", schema, childNodes);
    }

    private void checkAtLeastOneNode(S schema, Collection<Element> childNodes) {
        Preconditions.checkArgument(childNodes.isEmpty() == false,
                "Node detected 0 times, should be at least 1, identified by: %s, found: %s", schema, childNodes);
    }

    protected abstract DataSchemaNode getSchemaForChild(S schema, QName childPartialQName);

    protected abstract Map<QName, ChoiceNode> mapChildElementsFromChoices(S schema);

    protected abstract Map<QName, AugmentationSchema> mapChildElementsFromAugments(S schema);

    static final class NodeDispatcher {
        static final ContainerNodeDomParser CONTAINER_NODE_DOM_PARSER = new ContainerNodeDomParser();
        static final LeafNodeDomParser LEAF_NODE_DOM_PARSER = new LeafNodeDomParser();
        static final LeafSetNodeDomParser LEAF_SET_NODE_DOM_PARSER = new LeafSetNodeDomParser();
        static final MapNodeDomParser MAP_NODE_DOM_PARSER = new MapNodeDomParser();
        static final ChoiceNodeDomParser CHOICE_NODE_DOM_PARSER = new ChoiceNodeDomParser();
        static final AugmentationNodeDomParser AUGMENTATION_NODE_DOM_PARSER = new AugmentationNodeDomParser();

        static DataContainerChild<?, ?> dispatchChildElement(Object schema, List<Element> childNodes,
                XmlCodecProvider codecProvider) {
            Preconditions.checkArgument(childNodes.isEmpty() == false);

            if (schema instanceof ContainerSchemaNode) {
                return CONTAINER_NODE_DOM_PARSER.fromDom(childNodes, (ContainerSchemaNode) schema, codecProvider);
            } else if (schema instanceof LeafSchemaNode) {
                return LEAF_NODE_DOM_PARSER.fromDom(childNodes, (LeafSchemaNode) schema, codecProvider);
            } else if (schema instanceof LeafListSchemaNode) {
                return LEAF_SET_NODE_DOM_PARSER.fromDom(childNodes, (LeafListSchemaNode) schema, codecProvider);
            } else if (schema instanceof ListSchemaNode) {
                return MAP_NODE_DOM_PARSER.fromDom(childNodes, (ListSchemaNode) schema, codecProvider);
            } else if (schema instanceof ChoiceNode) {
                return CHOICE_NODE_DOM_PARSER.fromDom(childNodes, (ChoiceNode) schema, codecProvider);
            } else if (schema instanceof AugmentationSchema) {
                return AUGMENTATION_NODE_DOM_PARSER.fromDom(childNodes, (AugmentationSchema) schema, codecProvider);
            }

            throw new IllegalArgumentException("Unable to parse node, unknown schema type: " + schema);
        }
    }

}
