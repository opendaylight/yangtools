/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.*;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.AttributesBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

public abstract class BaseDispatcherParser<E, N extends DataContainerNode<?>, S>
        implements ToNormalizedNodeParser<E, N, S> {

    protected abstract DataContainerNodeBuilder<?, N> getBuilder(S schema);

    @Override
    public N parse(List<E> elements, S schema) {

        checkAtLeastOneNode(schema, elements);

        DataContainerNodeBuilder<?, N> containerBuilder = getBuilder(schema);

        // Map child nodes to QName
        LinkedListMultimap<QName, E> mappedChildElements = mapChildElements(elements);

        // Map child nodes from Augments
        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapChildElementsFromAugments(schema);
        LinkedListMultimap<AugmentationSchema, E> augmentsToElements = LinkedListMultimap.create();

        // Map child nodes from choices
        Map<QName, ChoiceNode> mappedChoiceChildNodes = mapChildElementsFromChoices(schema);
        LinkedListMultimap<ChoiceNode, E> choicesToElements = LinkedListMultimap.create();

        // process Child nodes
        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = getSchemaForChild(schema, childPartialQName);
            List<E> childrenForQName = mappedChildElements.get(childPartialQName);

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
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = getDispatcher()
                        .dispatchChildElement(childSchema, childrenForQName);
                containerBuilder.withChild(builtChildNode);
            }
        }

        // TODO ordering is not preserved for choice and augment elements
        for (ChoiceNode choiceSchema : choicesToElements.keySet()) {
            containerBuilder.withChild(getDispatcher().dispatchChildElement(choiceSchema,
                    choicesToElements.get(choiceSchema)));
        }

        for (AugmentationSchema augmentSchema : augmentsToElements.keySet()) {
            Set<DataSchemaNode> realChildSchemas = getRealSchemasForAugment(schema, augmentSchema);
            AugmentationSchemaProxy augSchemaProxy = new AugmentationSchemaProxy(augmentSchema, realChildSchemas);
            containerBuilder.withChild(getDispatcher().dispatchChildElement(augSchemaProxy, augmentsToElements.get(augmentSchema)));
        }

        if(containerBuilder instanceof AttributesBuilder){
            Preconditions.checkArgument(elements.size() == 1, "Unexpected number of elements: %s, should be 1 for: %s",
                    elements.size(), schema);
            ((AttributesBuilder) containerBuilder).withAttributes(getAttributes(elements.get(0)));
        }

        return containerBuilder.build();
    }

    protected Map<QName, String> getAttributes(E e) {
        return Collections.emptyMap();
    }

    private boolean isMarkedAs(Map<QName, ?> mappedAugmentChildNodes, QName qName) {
        return mappedAugmentChildNodes.containsKey(qName);
    }

    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema augmentSchema);

    protected abstract LinkedListMultimap<QName, E> mapChildElements(List<E> xml);

    protected void checkOnlyOneNode(S schema, Collection<E> childNodes) {
        Preconditions.checkArgument(childNodes.size() == 1,
                "Node detected multiple times, should be 1, identified by: %s, found: %s", schema, childNodes);
    }

    private void checkAtLeastOneNode(S schema, Collection<E> childNodes) {
        Preconditions.checkArgument(childNodes.isEmpty() == false,
                "Node detected 0 times, should be at least 1, identified by: %s, found: %s", schema, childNodes);
    }

    protected abstract DataSchemaNode getSchemaForChild(S schema, QName childPartialQName);

    protected abstract Map<QName, ChoiceNode> mapChildElementsFromChoices(S schema);

    protected abstract Map<QName, AugmentationSchema> mapChildElementsFromAugments(S schema);

    protected abstract NodeParserDispatcher<E> getDispatcher();


}
