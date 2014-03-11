/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

abstract class AbstractDispatcherSerializer<I extends InstanceIdentifier.PathArgument, N extends DataContainerNode<I>, S>
        implements DomSerializer<I, N, S> {

    @Override
    public List<Element> toDom(S schema, N node, XmlCodecProvider codec, Document doc) {
        List<Element> choiceChildren = Lists.newArrayList();

        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild : node.getValue()) {

            Object childSchema;

            if (choiceChild instanceof AugmentationNode) {

                AugmentationSchema augSchema = getAugmentedCase(schema, (AugmentationNode) choiceChild);
                Set<DataSchemaNode> realChildSchemas = getRealSchemasForAugment(schema, augSchema);
                childSchema = new AugmentationSchemaProxy(augSchema, realChildSchemas);

            } else {
                childSchema = getSchemaForChild(schema, choiceChild);
            }

            List<Element> childElements = NodeDispatcher.dispatchChildElement(childSchema, choiceChild, codec, doc);
            for (Element childElement : childElements) {
                choiceChildren.add(childElement);
            }
        }

        return choiceChildren;
    }

    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema childSchema);

    protected abstract DataSchemaNode getSchemaForChild(S schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild);

    protected abstract AugmentationSchema getAugmentedCase(S schema, AugmentationNode choiceChild);

    static final class NodeDispatcher {

        static final ContainerNodeDomSerializer CONTAINER_NODE_DOM_SERIALIZER = new ContainerNodeDomSerializer();
        static final LeafNodeDomSerializer LEAF_NODE_DOM_SERIALIZER = new LeafNodeDomSerializer();
        static final LeafSetNodeDomSerializer LEAF_SET_NODE_DOM_SERIALIZER = new LeafSetNodeDomSerializer();
        static final MapNodeDomSerializer MAP_NODE_DOM_SERIALIZER = new MapNodeDomSerializer();
        static final ChoiceNodeDomSerializer CHOICE_NODE_DOM_SERIALIZER = new ChoiceNodeDomSerializer();
        public static final AugmentationNodeDomSerializer AUGMENTATION_NODE_DOM_SERIALIZER = new AugmentationNodeDomSerializer();

        static List<Element> dispatchChildElement(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            if (dataContainerChild instanceof ContainerNode) {
                return onContainerNode(childSchema, dataContainerChild, codecProvider, doc);
            } else if (dataContainerChild instanceof LeafNode<?>) {
                return onLeafNode(childSchema, dataContainerChild, codecProvider, doc);
            } else if (dataContainerChild instanceof MixinNode) {
                if (dataContainerChild instanceof LeafSetNode<?>) {
                    return onLeafListNode(childSchema, dataContainerChild, codecProvider, doc);
                } else if (dataContainerChild instanceof MapNode) {
                    return onListNode(childSchema, dataContainerChild, codecProvider, doc);
                } else if (dataContainerChild instanceof ChoiceNode) {
                    return onChoiceNode(childSchema, dataContainerChild, codecProvider, doc);
                } else if (dataContainerChild instanceof AugmentationNode) {
                    return onAugmentationSchema(childSchema, dataContainerChild, codecProvider, doc);
                }
            }
            throw new IllegalArgumentException("Unable to serialize " + childSchema);
        }

        private static List<Element> onAugmentationSchema(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, AugmentationSchema.class, dataContainerChild);
            return AUGMENTATION_NODE_DOM_SERIALIZER.toDom((AugmentationSchema) childSchema,
                    (AugmentationNode) dataContainerChild, codecProvider, doc);
        }

        private static List<Element> onChoiceNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, org.opendaylight.yangtools.yang.model.api.ChoiceNode.class,
                    dataContainerChild);
            return CHOICE_NODE_DOM_SERIALIZER.toDom((org.opendaylight.yangtools.yang.model.api.ChoiceNode) childSchema,
                    (ChoiceNode) dataContainerChild, codecProvider, doc);
        }

        private static List<Element> onListNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, ListSchemaNode.class, dataContainerChild);
            return MAP_NODE_DOM_SERIALIZER.toDom((ListSchemaNode) childSchema, (MapNode) dataContainerChild,
                    codecProvider, doc);
        }

        private static List<Element> onLeafListNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, LeafListSchemaNode.class, dataContainerChild);
            return LEAF_SET_NODE_DOM_SERIALIZER.toDom((LeafListSchemaNode) childSchema,
                    (LeafSetNode<?>) dataContainerChild, codecProvider, doc);
        }

        private static List<Element> onLeafNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, LeafSchemaNode.class, dataContainerChild);
            List<Element> elements = LEAF_NODE_DOM_SERIALIZER.toDom((LeafSchemaNode) childSchema,
                    (LeafNode<?>) dataContainerChild, codecProvider, doc);
            checkOnlyOneSerializedElement(elements, dataContainerChild);
            return elements;
        }

        private static void checkOnlyOneSerializedElement(List<Element> elements,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            Preconditions.checkArgument(elements.size() == 1,
                    "Unexpected count of dom elements for leaf-list entry serialized from: %s, should be 1, was: %s",
                    dataContainerChild, elements.size());
        }

        private static List<Element> onContainerNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
                XmlCodecProvider codecProvider, Document doc) {
            checkSchemaCompatibility(childSchema, ContainerSchemaNode.class, dataContainerChild);

            List<Element> elements = CONTAINER_NODE_DOM_SERIALIZER.toDom((ContainerSchemaNode) childSchema,
                    (ContainerNode) dataContainerChild, codecProvider, doc);
            checkOnlyOneSerializedElement(elements, dataContainerChild);
            return elements;
        }

        private static void checkSchemaCompatibility(Object childSchema, Class<?> containerSchemaNodeClass,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            Preconditions.checkArgument(containerSchemaNodeClass.isAssignableFrom(childSchema.getClass()),
                    "Incompatible schema: %s with node: %s, expected: %s", childSchema, dataContainerChild,
                    containerSchemaNodeClass);
        }
    }

}
