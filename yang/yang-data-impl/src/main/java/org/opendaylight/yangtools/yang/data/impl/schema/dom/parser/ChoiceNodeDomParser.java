/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
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

public class ChoiceNodeDomParser {

    public ChoiceNode fromDomElements(Collection<Element> xml, org.opendaylight.yangtools.yang.model.api.ChoiceNode schema, XmlCodecProvider codecProvider) {
        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> containerBuilder = Builders.choiceBuilder(schema);

        Multimap<QName, Element> mappedChildElements = DomUtils.mapChildElements(xml);

        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapAugments(schema);
        Map<AugmentationSchema, Collection<Element>> augmentsToElements = Maps.newLinkedHashMap();

        Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mappedChoiceChildNodes = mapChoices(schema);
        Map<org.opendaylight.yangtools.yang.model.api.ChoiceNode, Collection<Element>> choicesToElements = Maps.newLinkedHashMap();

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
                org.opendaylight.yangtools.yang.model.api.ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
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

        for (org.opendaylight.yangtools.yang.model.api.ChoiceNode choiceSchema : choicesToElements.keySet()) {
            containerBuilder.withChild(dispatchChildElement(choiceSchema, choicesToElements.get(choiceSchema), codecProvider));
        }
        for (AugmentationSchema augmentSchema : augmentsToElements.keySet()) {
            containerBuilder.withChild(new AugmentationNodeDomParser().fromDomElements(augmentsToElements.get(augmentSchema), augmentSchema, codecProvider));
        }

        return containerBuilder.build();
    }

    private Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mapChoices( schema) {
        Map<QName, org.opendaylight.yangtools.yang.model.api.ChoiceNode> mappedChoices = Maps.newHashMap();

        for (DataSchemaNode childSchema : schema.getChildNodes()) {
            if(childSchema instanceof org.opendaylight.yangtools.yang.model.api.ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((org.opendaylight.yangtools.yang.model.api.ChoiceNode) childSchema).getCases()) {
                    for (DataSchemaNode caseChild : choiceCaseNode.getChildNodes()) {
                        mappedChoices.put(caseChild.getQName(), (org.opendaylight.yangtools.yang.model.api.ChoiceNode) childSchema);
                    }
                }
            }
        }

        return mappedChoices;
    }

    private Map<QName, AugmentationSchema> mapAugments(AugmentationTarget schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newHashMap();

        for (AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (DataSchemaNode augmentationChild : augmentationSchema.getChildNodes()) {
                mappedAugments.put(augmentationChild.getQName(), augmentationSchema);
            }
        }

        return mappedAugments;
    }
}
