/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;

/**
 * Abstract(base) Parser for DataContainerNodes e.g. ContainerNode, AugmentationNode.
 */
public abstract class BaseDispatcherParser<E, N extends DataContainerNode<?>, S>
        implements ToNormalizedNodeParser<E, N, S> {

    /**
     *
     * @param schema
     * @return New(empty) instance of a builder to build node identified by schema.
     */
    protected abstract DataContainerNodeBuilder<?, N> getBuilder(S schema);

    /**
     *
     * @param schema
     * @param childQName QName of a child being parsed, QName does not continue revision date
     * @return schema object for child identified by parent schema: schema and QName childQName
     */
    protected abstract DataSchemaNode getSchemaForChild(S schema, QName childQName);

    /**
     *
     * @param xml
     * @return map from QName to child elements. Multiple elements are allowed under QName.
     */
    protected abstract LinkedListMultimap<QName, E> mapChildElements(Iterable<E> xml);

    /**
     *
     * @param schema
     * @return map from QName to ChoiceNode schema of child nodes that are
     *         contained within a choice statement under current schema.
     */
    protected abstract Map<QName, ChoiceNode> mapChildElementsFromChoices(S schema);

    /**
     *
     * @param schema
     * @return map from QName to child elements that are added by augmentation
     *         that targets current schema.
     */
    protected abstract Map<QName, AugmentationSchema> mapChildElementsFromAugments(S schema);

    /**
     *
     * @param schema
     * @param augmentSchema
     * @return Set of real schema objects that represent child nodes of an
     *         augmentation. Augmentation schema child nodes, if further
     *         augmented, do not contain further augmented, that are crucial for
     *         parsing. The real schema object can be retrieved from parent schema: schema.
     */
    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema augmentSchema);

    /**
     *
     * @return dispatcher object to dispatch parsing of child elements, might be
     *         the same instance if provided parsers are immutable.
     */
    protected abstract NodeParserDispatcher<E> getDispatcher();

    @Override
    public N parse(Iterable<E> element, S schema) {

        checkAtLeastOneNode(schema, element);

        DataContainerNodeBuilder<?, N> containerBuilder = getBuilder(schema);

        // Map child nodes to QName
        LinkedListMultimap<QName, E> mappedChildElements = mapChildElements(element);

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

        return containerBuilder.build();
    }

    private boolean isMarkedAs(Map<QName, ?> mappedAugmentChildNodes, QName qName) {
        return mappedAugmentChildNodes.containsKey(qName);
    }

    protected void checkOnlyOneNode(S schema, Iterable<E> childNodes) {
        final int size = Iterables.size(childNodes);
        Preconditions.checkArgument(size == 1,
                "Node detected multiple times, should be 1, identified by: %s, found: %s", schema, childNodes);
    }

    private void checkAtLeastOneNode(S schema, Iterable<E> childNodes) {
        Preconditions.checkArgument(Iterables.isEmpty(childNodes) == false,
                "Node detected 0 times, should be at least 1, identified by: %s, found: %s", schema, childNodes);
    }
}
