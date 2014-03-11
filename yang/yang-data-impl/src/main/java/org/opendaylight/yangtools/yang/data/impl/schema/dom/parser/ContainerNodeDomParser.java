/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

public class ContainerNodeDomParser extends AbstractDispatcherParser {

    public ContainerNode fromDomElement(Element xml, ContainerSchemaNode schema,
                                               XmlCodecProvider codecProvider) {

        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder = Builders.containerBuilder(schema);

        Multimap<QName, Element> mappedChildElements = DomUtils.mapChildElements(xml.getChildNodes());

        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapAugments(schema);
        Map<AugmentationSchema, Collection<Element>> augmentsToElements = Maps.newLinkedHashMap();

        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChoices(schema);
        Map<ChoiceNode, Collection<Element>> choicesToElements = Maps.newLinkedHashMap();

        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = DomUtils.findSchemaForChild(schema, childPartialQName);
            Collection<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            // Augment
            if(mappedAugmentChildNodes.containsKey(childSchema.getQName())) {
                AugmentationSchema augmentationSchema = mappedAugmentChildNodes.get(childSchema.getQName());
                Collection<Element> augmentElements = augmentsToElements.get(augmentationSchema);
                if(augmentElements==null) {
                    augmentElements = Lists.newArrayList();
                    augmentsToElements.put(augmentationSchema, augmentElements);
                }
                augmentElements.addAll(childrenForQName);

                // Choices
            } else if(mappedChoiceChildNodes.containsKey(childSchema.getQName())) {
                ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                Collection<Element> choiceElements = choicesToElements.get(choiceSchema);
                if(choiceElements==null) {
                    choiceElements = Lists.newArrayList();
                    choicesToElements.put(choiceSchema, choiceElements);
                }
                choiceElements.addAll(childrenForQName);

            } else {
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = dispatchChildElement(
                        childSchema, childrenForQName, codecProvider);
                containerBuilder.withChild(builtChildNode);
            }
        }

        for (ChoiceNode choiceSchema : choicesToElements.keySet()) {
            containerBuilder.withChild(dispatchChildElement(choiceSchema, choicesToElements.get(choiceSchema), codecProvider));
        }
        for (AugmentationSchema augmentSchema : augmentsToElements.keySet()) {
            containerBuilder.withChild(new AugmentationNodeDomParser().fromDomElements(augmentsToElements.get(augmentSchema), augmentSchema, codecProvider));
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

    // TODO refactor
    static DataContainerChild<?, ?> dispatchChildElement(DataSchemaNode schema, Collection<Element> childNodes,
            XmlCodecProvider codecProvider) {
        if (schema instanceof ContainerSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Container node detected multiple times, identified by: %s, found: %s", schema.getQName(),
                    childNodes);
            return new ContainerNodeDomParser().fromDomElement(childNodes.iterator().next(),
                    (ContainerSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Leaf node detected multiple times, identified by: %s, found: %s", schema.getQName(), childNodes);
            return new LeafNodeDomParser().fromDomElement(childNodes.iterator().next(), (LeafSchemaNode) schema,
                    codecProvider);
        } else if (schema instanceof LeafListSchemaNode) {
            return new LeafSetNodeDomParser().fromDomElements(childNodes, (LeafListSchemaNode) schema, codecProvider);
        } else if (schema instanceof ListSchemaNode) {
            return new MapNodeDomParser().fromDomElements(childNodes, (ListSchemaNode) schema, codecProvider);
        } else if (schema instanceof org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode) {
            return new ChoiceNodeDomParser().fromDomElements(childNodes,
                    (org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode) schema, codecProvider);
        }

        throw new IllegalArgumentException("Unable to build from " + schema);
    }
}
