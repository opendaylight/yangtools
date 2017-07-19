/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.AttributesBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

/**
 * Abstract(base) Parser for DataContainerNodes e.g. ContainerNode, AugmentationNode.
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class BaseDispatcherParser<E, P extends YangInstanceIdentifier.PathArgument, N extends DataContainerNode<P>, S>
        implements ExtensibleParser<P, E, N, S> {

    private final BuildingStrategy<P, N> buildingStrategy;

    public BaseDispatcherParser(final BuildingStrategy<P, N> buildingStrategy) {
        this.buildingStrategy = buildingStrategy;
    }

    public BaseDispatcherParser() {
        this.buildingStrategy = new SimpleBuildingStrategy<>();
    }

    /**
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @return New(empty) instance of a builder to build node identified by schema.
     */
    protected abstract DataContainerNodeBuilder<P, N> getBuilder(S schema);

    /**
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @param childQName QName of a child being parsed, QName does not continue revision date
     * @return schema object for child identified by parent schema: schema and QName childQName
     */
    protected abstract DataSchemaNode getSchemaForChild(S schema, QName childQName);

    /**
     *
     * @param elements elements to be parsed into NormalizedNode
     * @return map from QName to child elements. Multiple elements are allowed under QName.
     */
    protected abstract LinkedListMultimap<QName, E> mapChildElements(Iterable<E> elements);

    /**
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @return map from QName to ChoiceNode schema of child nodes that are
     *         contained within a choice statement under current schema.
     */
    protected abstract Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(S schema);

    /**
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @return map from QName to child elements that are added by augmentation
     *         that targets current schema.
     */
    protected abstract Map<QName, AugmentationSchema> mapChildElementsFromAugments(S schema);

    /**
     *
     * @param schema schema belonging to the type N of NormalizedNode
     * @param augmentSchema augmentSchema
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

    /**
     * can return null only if you override ParsingStrategy and explicitely return null
     * @param elements elements to be parsed into NormalizedNode
     * @param schema schema belonging to the type N of NormalizedNode
     * @return child of DataContainerNode as a result of parsing list of E elements with schema S
     */
    @Nullable
    @Override
    public N parse(final Iterable<E> elements, final S schema) {

        checkAtLeastOneNode(schema, elements);

        DataContainerNodeBuilder<P, N> containerBuilder = getBuilder(schema);

        // Map child nodes to QName
        LinkedListMultimap<QName, E> mappedChildElements = mapChildElements(elements);

        // Map child nodes from Augments
        Map<QName, AugmentationSchema> mappedAugmentChildNodes = mapChildElementsFromAugments(schema);
        LinkedListMultimap<AugmentationSchema, E> augmentsToElements = LinkedListMultimap.create();

        // Map child nodes from choices
        Map<QName, ChoiceSchemaNode> mappedChoiceChildNodes = mapChildElementsFromChoices(schema);
        LinkedListMultimap<ChoiceSchemaNode, E> choicesToElements = LinkedListMultimap.create();

        Map<QName, String> attributes = getAttributes(elements.iterator().next());
        if (containerBuilder instanceof AttributesBuilder) {
            final int size = Iterables.size(elements);
            Preconditions.checkArgument(size == 1, "Unexpected number of elements: %s, should be 1 for: %s",
                    size, schema);
            ((AttributesBuilder<?>) containerBuilder).withAttributes(attributes);
        }

        //parse keys first
        if (schema instanceof ListSchemaNode) {
            for (QName qname : ((ListSchemaNode) schema).getKeyDefinition()) {
                final QName noRev = qname.withoutRevision();
                if (mappedChildElements.get(noRev).isEmpty()) {
                    continue;
                }

                DataSchemaNode childSchema = getSchemaForChild(schema, qname);
                List<E> childrenForQName = mappedChildElements.removeAll(noRev);

                DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> optionalChildNode = getDispatcher()
                        .dispatchChildElement(childSchema, childrenForQName);
                if (optionalChildNode != null) {
                    containerBuilder.withChild(optionalChildNode);
                }
            }
        }

        //stage attribues for strategy before going deeper in the recursion
        buildingStrategy.prepareAttributes(attributes, containerBuilder);

        // process Child nodes
        for (QName childPartialQName : mappedChildElements.keySet()) {
            DataSchemaNode childSchema = getSchemaForChild(schema, childPartialQName);
            //with strict parsing an exception would be already thrown, with nonstrict we want to ignore this node
            if (childSchema == null) {
                continue;
            }
            List<E> childrenForQName = mappedChildElements.get(childPartialQName);

            // Augment
            if (isMarkedAs(mappedAugmentChildNodes, childSchema.getQName())) {
                AugmentationSchema augmentationSchema = mappedAugmentChildNodes.get(childSchema.getQName());
                augmentsToElements.putAll(augmentationSchema, childrenForQName);
                // Choices
            } else if (isMarkedAs(mappedChoiceChildNodes, childSchema.getQName())) {
                ChoiceSchemaNode choiceSchema = mappedChoiceChildNodes.get(childSchema.getQName());
                choicesToElements.putAll(choiceSchema, childrenForQName);
                // Regular child nodes
            } else {
                DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> optionalChildNode = getDispatcher()
                        .dispatchChildElement(childSchema, childrenForQName);
                if (optionalChildNode != null) {
                    containerBuilder.withChild(optionalChildNode);
                }
            }
        }

        // TODO ordering is not preserved for choice and augment elements
        for (ChoiceSchemaNode choiceSchema : choicesToElements.keySet()) {
            DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> optionalChild = getDispatcher()
                    .dispatchChildElement(choiceSchema, choicesToElements.get(choiceSchema));
            if (optionalChild != null) {
                containerBuilder.withChild(optionalChild);
            }
        }

        for (AugmentationSchema augmentSchema : augmentsToElements.keySet()) {
            Set<DataSchemaNode> realChildSchemas = getRealSchemasForAugment(schema, augmentSchema);
            EffectiveAugmentationSchema augSchemaProxy = new EffectiveAugmentationSchema(augmentSchema, realChildSchemas);
            DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> optionalChild = getDispatcher()
                    .dispatchChildElement(augSchemaProxy, augmentsToElements.get(augmentSchema));
            if (optionalChild != null) {
                containerBuilder.withChild(optionalChild);
            }
        }

        return buildingStrategy.build(containerBuilder);
    }

    @Override
    public BuildingStrategy<P, N> getBuildingStrategy() {
        return buildingStrategy;
    }

    protected Map<QName, String> getAttributes(final E e) {
        return Collections.emptyMap();
    }

    protected boolean strictParsing() {
        return true;
    }

    private static boolean isMarkedAs(final Map<QName, ?> mappedAugmentChildNodes, final QName qName) {
        return mappedAugmentChildNodes.containsKey(qName);
    }

    protected void checkOnlyOneNode(final S schema, final Iterable<E> childNodes) {
        final int size = Iterables.size(childNodes);
        Preconditions.checkArgument(size == 1,
                "Node detected multiple times, should be 1, identified by: %s, found: %s", schema, childNodes);
    }

    private void checkAtLeastOneNode(final S schema, final Iterable<E> childNodes) {
        Preconditions.checkArgument(!Iterables.isEmpty(childNodes),
                "Node detected 0 times, should be at least 1, identified by: %s, found: %s", schema, childNodes);
    }

    public static class SimpleBuildingStrategy<P extends YangInstanceIdentifier.PathArgument, N extends DataContainerNode<P>> implements BuildingStrategy<P, N> {
        @Override
        public N build(final NormalizedNodeBuilder<P, ?, N> builder) {
            return builder.build();
        }

        @Override
        public void prepareAttributes(final Map<QName, String> attributes, final NormalizedNodeBuilder<P, ?, N> containerBuilder) {
            // NOOP
        }
    }
}
