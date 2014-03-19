/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.model.api.*;

import java.util.List;

public interface NodeSerializerDispatcher<E> {
    List<E> dispatchChildElement(Object childSchema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild);

    public static abstract class BaseNodeSerializerDispatcher<E> implements NodeSerializerDispatcher<E> {

        public List<E> dispatchChildElement(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            if (dataContainerChild instanceof ContainerNode) {
                return onContainerNode(childSchema, dataContainerChild);
            } else if (dataContainerChild instanceof LeafNode<?>) {
                return onLeafNode(childSchema, dataContainerChild);
            } else if (dataContainerChild instanceof MixinNode) {
                if (dataContainerChild instanceof LeafSetNode<?>) {
                    return onLeafListNode(childSchema, dataContainerChild);
                } else if (dataContainerChild instanceof MapNode) {
                    return onListNode(childSchema, dataContainerChild);
                } else if (dataContainerChild instanceof ChoiceNode) {
                    return onChoiceNode(childSchema, dataContainerChild);
                } else if (dataContainerChild instanceof AugmentationNode) {
                    return onAugmentationSchema(childSchema, dataContainerChild);
                }
            }
            throw new IllegalArgumentException("Unable to serialize " + childSchema);
        }

        private List<E> onAugmentationSchema(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, AugmentationSchema.class, dataContainerChild);
            return getAugmentationNodeSerializer().serialize((AugmentationSchema) childSchema,
                    (AugmentationNode) dataContainerChild);
        }

        private List<E> onChoiceNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, org.opendaylight.yangtools.yang.model.api.ChoiceNode.class,
                    dataContainerChild);
            return getChoiceNodeSerializer()
                    .serialize((org.opendaylight.yangtools.yang.model.api.ChoiceNode) childSchema,
                            (ChoiceNode) dataContainerChild);
        }

        private List<E> onListNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, ListSchemaNode.class, dataContainerChild);
            return getMapNodeSerializer().serialize((ListSchemaNode) childSchema, (MapNode) dataContainerChild);
        }

        private List<E> onLeafListNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, LeafListSchemaNode.class, dataContainerChild);
            return getLeafSetNodeSerializer().serialize((LeafListSchemaNode) childSchema,
                    (LeafSetNode<?>) dataContainerChild);
        }

        private List<E> onLeafNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, LeafSchemaNode.class, dataContainerChild);
            List<E> elements = getLeafNodeSerializer().serialize((LeafSchemaNode) childSchema,
                    (LeafNode<?>) dataContainerChild);
            checkOnlyOneSerializedElement(elements, dataContainerChild);
            return elements;
        }

        private static void checkOnlyOneSerializedElement(List<?> elements,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            Preconditions.checkArgument(elements.size() == 1,
                    "Unexpected count of elements for entry serialized from: %s, should be 1, was: %s",
                    dataContainerChild, elements.size());
        }

        private List<E> onContainerNode(Object childSchema,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            checkSchemaCompatibility(childSchema, ContainerSchemaNode.class, dataContainerChild);

            List<E> elements = getContainerNodeSerializer().serialize((ContainerSchemaNode) childSchema,
                    (ContainerNode) dataContainerChild);
            checkOnlyOneSerializedElement(elements, dataContainerChild);
            return elements;
        }

        private static void checkSchemaCompatibility(Object childSchema, Class<?> containerSchemaNodeClass,
                DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild) {
            Preconditions.checkArgument(containerSchemaNodeClass.isAssignableFrom(childSchema.getClass()),
                    "Incompatible schema: %s with node: %s, expected: %s", childSchema, dataContainerChild,
                    containerSchemaNodeClass);
        }

        protected abstract FromNormalizedNodeSerializer<E, ContainerNode, ContainerSchemaNode> getContainerNodeSerializer();

        protected abstract FromNormalizedNodeSerializer<E, LeafNode<?>, LeafSchemaNode> getLeafNodeSerializer();

        protected abstract FromNormalizedNodeSerializer<E, LeafSetNode<?>, LeafListSchemaNode> getLeafSetNodeSerializer();

        protected abstract FromNormalizedNodeSerializer<E, MapNode, ListSchemaNode> getMapNodeSerializer();

        protected abstract FromNormalizedNodeSerializer<E, org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> getChoiceNodeSerializer();

        protected abstract FromNormalizedNodeSerializer<E, AugmentationNode, AugmentationSchema> getAugmentationNodeSerializer();
    }
}
