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
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;

public class AugmentationNodeDomParser {


    public AugmentationNode fromDomElements(Collection<Element> xml, AugmentationSchema schema,
                                        XmlCodecProvider codecProvider) {

        DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> containerBuilder = Builders.augmentationBuilder(schema);

        Multimap<QName, Element> mappedChildElements = DomUtils.mapChildElements(xml);

        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChoices(schema);
        Map<ChoiceNode, Collection<Element>> choicesToElements = Maps.newLinkedHashMap();

        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = DomUtils.findSchemaForChild(schema, childPartialQName);
            Collection<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            if(mappedChoiceChildNodes.containsKey(childSchema.getQName())) {
                ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                Collection<Element> choiceElements = choicesToElements.get(choiceSchema);
                if(choiceElements==null) {
                    choiceElements = Lists.newArrayList();
                    choicesToElements.put(choiceSchema, choiceElements);
                }
                choiceElements.addAll(childrenForQName);

            } else {
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = ContainerNodeDomParser.dispatchChildElement(
                        childSchema, childrenForQName, codecProvider);
                containerBuilder.withChild(builtChildNode);
            }
        }

        for (ChoiceNode choiceSchema : choicesToElements.keySet()) {
            ContainerNodeDomParser.dispatchChildElement(choiceSchema, choicesToElements.get(choiceSchema), codecProvider);
        }

        return containerBuilder.build();
    }

    private Map<QName, ChoiceNode> mapChoices(AugmentationSchema schema) {
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

    public AugmentationNode fromDomElements(Collection<Element> xml, AugmentationSchema schema) {
        return fromDomElements(xml, schema, DomUtils.defaultValueCodecProvider());
    }
}
