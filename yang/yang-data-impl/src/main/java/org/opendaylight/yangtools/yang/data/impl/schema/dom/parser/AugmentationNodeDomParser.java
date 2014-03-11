/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

import com.google.common.collect.LinkedListMultimap;

public final class AugmentationNodeDomParser {

    // TODO refactor along with serializer and especially DOmUtils

    public AugmentationNode fromDom(List<Element> xml, AugmentationSchema schema,
            Set<DataSchemaNode> realChildSchemas, XmlCodecProvider codecProvider) {

        DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> containerBuilder = Builders
                .augmentationBuilder(schema);

        // Map child nodes to QName
        LinkedListMultimap<QName, Element> mappedChildElements = mapChildElements(xml);

        // Map child nodes from choices
        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChildElementsFromChoices(schema, realChildSchemas);
        LinkedListMultimap<ChoiceNode, Element> choicesToElements = LinkedListMultimap.create();

         // process Child nodes
        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = DomUtils.findSchemaForChild(null, childPartialQName, realChildSchemas);
            List<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            if (isMarkedAs(mappedChoiceChildNodes, childSchema.getQName())) {
                ChoiceNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                choicesToElements.putAll(choiceSchema, childrenForQName);
                // Regular child nodes
            } else {
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = AbstractDispatcherParser.NodeDispatcher
                        .dispatchChildElement(childSchema, childrenForQName, codecProvider);
                containerBuilder.withChild(builtChildNode);
            }
        }

        // Augments and choices are processed at the end, when all child nodes
        // are present

        // TODO ordering is not preserved
        // TODO how to preserve ordering between case/augment child nodes and
        // regular nodes

        for (ChoiceNode choiceSchema : choicesToElements.keySet()) {
            containerBuilder.withChild(AbstractDispatcherParser.NodeDispatcher.dispatchChildElement(choiceSchema,
                    choicesToElements.get(choiceSchema), codecProvider));
        }

        return containerBuilder.build();
    }

    private DataSchemaNode getRealSchema(Set<DataSchemaNode> realChildSchemas, QName childPartialQName) {
        return DomUtils.findSchemaForChild(null, childPartialQName, realChildSchemas);
    }

    private boolean isMarkedAs(Map<QName, ?> mappedAugmentChildNodes, QName qName) {
        return mappedAugmentChildNodes.containsKey(qName);
    }

    protected LinkedListMultimap<QName, Element> mapChildElements(List<Element> xml) {
        return DomUtils.mapChildElements(xml);
    }

    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(AugmentationSchema schema, Set<DataSchemaNode> realChildSchemas) {
        return DomUtils.mapChildElementsFromChoicesInAugment(schema, realChildSchemas);
    }

}
