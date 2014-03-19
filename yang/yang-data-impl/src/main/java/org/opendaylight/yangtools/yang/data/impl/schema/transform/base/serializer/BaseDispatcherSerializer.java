/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.FromNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.*;

import com.google.common.collect.Lists;

/**
 * Abstract(base) Serializer for DataContainerNodes e.g. ContainerNode, AugmentationNode.
 */
public abstract class BaseDispatcherSerializer<E, N extends DataContainerNode<?>, S> implements
        FromNormalizedNodeSerializer<E, N, S> {

    /**
     *
     * @param schema
     * @param augmentationSchema
     * @return Set of real schema objects that represent child nodes of an
     *         augmentation. Augmentation schema child nodes, if further
     *         augmented, do not contain further augmented, that are crucial for
     *         parsing. The real schema object can be retrieved from parent schema: schema.
     */
    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema augmentationSchema);

    /**
     *
     * @param schema
     * @param childNode
     * @return Schema object associated with child node identified as: childNode.
     *         Schema should be retrieved from parent schema: schema.
     */
    protected abstract DataSchemaNode getSchemaForChild(S schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> childNode);

    /**
     *
     * @param schema
     * @param augmentationNode
     * @return Schema object associated with augmentation child node identified as: augmentationNode.
     *         Schema should be retrieved from parent schema: schema.
     */
    protected abstract AugmentationSchema getAugmentedCase(S schema, AugmentationNode augmentationNode);

    /**
     *
     * @return Dispatcher object to dispatch serialization of child elements, might be
     *         the same instance if provided serializers are immutable.
     */
    protected abstract NodeSerializerDispatcher<E> getNodeDispatcher();

    @Override
    public List<E> serialize(S schema, N node) {
        List<E> choiceChildren = Lists.newArrayList();

        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild : node.getValue()) {

            Object childSchema;

            if (choiceChild instanceof AugmentationNode) {

                AugmentationSchema augSchema = getAugmentedCase(schema, (AugmentationNode) choiceChild);
                Set<DataSchemaNode> realChildSchemas = getRealSchemasForAugment(schema, augSchema);
                childSchema = new AugmentationSchemaProxy(augSchema, realChildSchemas);

            } else {
                childSchema = getSchemaForChild(schema, choiceChild);
            }

            List<E> childElements = getNodeDispatcher().dispatchChildElement(childSchema, choiceChild);
            for (E childElement : childElements) {
                choiceChildren.add(childElement);
            }
        }

        return choiceChildren;
    }
}