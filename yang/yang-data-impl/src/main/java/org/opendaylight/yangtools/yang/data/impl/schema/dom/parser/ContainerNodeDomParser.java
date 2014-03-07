/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

public class ContainerNodeDomParser extends AbstractDispatcherParser {

    public ContainerNode fromDomElement(Element xml, ContainerSchemaNode schema,
                                               XmlCodecProvider codecProvider) {

        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder = Builders.containerBuilder(schema);

        // TODO refactor + redesign augment/choice processing ... reuse it then in choices, augments

        Multimap<QName, Element> mappedChildElements = DomUtils.mapChildElements(xml.getChildNodes());
        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapAugments(schema);
        Map<AugmentationSchema, DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode>> augmentBuilders = Maps.newLinkedHashMap();

        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChoices(schema);
        Map<ChoiceNode, DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode>> choiceBuilders = Maps.newLinkedHashMap();

        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = DomUtils.findSchemaForChild(schema, childPartialQName);
            Collection<Element> childrenForQName = mappedChildElements.get(childPartialQName);
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = dispatchChildElement(
                    childSchema, childrenForQName, codecProvider);

            // Augment
            if(mappedAugmentChildNodes.containsKey(childSchema.getQName())) {

                AugmentationSchema augmentSchema = mappedAugmentChildNodes.get(childSchema.getQName());
                DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentBuilder = augmentBuilders.get(augmentSchema);
                if(augmentBuilder==null) {
                    augmentBuilder = Builders.augmentationBuilder(augmentSchema);
                    augmentBuilders.put(augmentSchema, augmentBuilder);
                }

                augmentBuilder.withChild(builtChildNode);

                // Choices
            } else if(mappedChoiceChildNodes.containsKey(childSchema.getQName())) {
                ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> choiceBuilder = choiceBuilders.get(choiceSchema);
                if(choiceBuilder==null) {
                    choiceBuilder = Builders.choiceBuilder(choiceSchema);
                    choiceBuilders.put(choiceSchema, choiceBuilder);
                }

                choiceBuilder.withChild(builtChildNode);
                // Regular child node
            } else {
                containerBuilder.withChild(builtChildNode);
            }
        }

        for (DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentBuilder : augmentBuilders.values()) {
            containerBuilder.withChild(augmentBuilder.build());
        }
        for (DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode> choiceBuilder : choiceBuilders.values()) {
            containerBuilder.withChild(choiceBuilder.build());
        }

        return containerBuilder.build();
    }

    private Map<QName, ChoiceNode> mapChoices(ContainerSchemaNode schema) {
        Map<QName, ChoiceNode> mappedChoices = Maps.newHashMap();

        for (DataSchemaNode childSchema : schema.getChildNodes()) {
            if(childSchema instanceof ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) childSchema).getCases()) {
                    for (DataSchemaNode caseChild : choiceCaseNode.getChildNodes()) {
                        mappedChoices.put(caseChild.getQName(), (ChoiceNode) childSchema);
                    }
                }
            }
        }

        return mappedChoices;
    }

    private Map<QName, AugmentationSchema> mapAugments(ContainerSchemaNode schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newHashMap();

        for (AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (DataSchemaNode augmentationChild : augmentationSchema.getChildNodes()) {
                mappedAugments.put(augmentationChild.getQName(), augmentationSchema);
            }
        }

        return mappedAugments;
    }

    public ContainerNode fromDomElement(Element xml, ContainerSchemaNode schema) {
        return fromDomElement(xml, schema, DomUtils.defaultValueCodecProvider());
    }

    static DataContainerChild<?, ?> dispatchChildElement(DataSchemaNode schema, Collection<Element> childNodes,
                                                                 XmlCodecProvider codecProvider) {
        if (schema instanceof ContainerSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Container node detected multiple times, identified by: %s, found: %s", schema.getQName(),
                    childNodes);
            return new ContainerNodeDomParser().fromDomElement(childNodes.iterator().next(), (ContainerSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Leaf node detected multiple times, identified by: %s, found: %s", schema.getQName(),
                    childNodes);
            return new LeafNodeDomParser().fromDomElement(childNodes.iterator().next(), (LeafSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafListSchemaNode) {
            return new LeafSetNodeDomParser().fromDomElements(childNodes, (LeafListSchemaNode) schema, codecProvider);
        } else if (schema instanceof ListSchemaNode) {
            return new MapNodeDomParser().fromDomElements(childNodes, (ListSchemaNode) schema, codecProvider);
        }

        throw new IllegalArgumentException("Unable to build from " + schema);
    }
}
