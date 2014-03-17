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

public abstract class BaseDispatcherSerializer<E, N extends DataContainerNode<?>, S> implements
        FromNormalizedNodeSerializer<E, N, S> {

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

    protected abstract Set<DataSchemaNode> getRealSchemasForAugment(S schema, AugmentationSchema childSchema);

    protected abstract DataSchemaNode getSchemaForChild(S schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild);

    protected abstract AugmentationSchema getAugmentedCase(S schema, AugmentationNode choiceChild);

    protected abstract NodeSerializerDispatcher<E> getNodeDispatcher();

}
