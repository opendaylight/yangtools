/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer;

import java.util.Set;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import com.google.common.collect.Sets;

/**
 * Abstract(base) serializer for ChoiceNodes, serializes elements of type E.
 *
 * @param <E> type of serialized elements
 */
public abstract class ChoiceNodeBaseSerializer<E>
        extends
        BaseDispatcherSerializer<E, ChoiceNode, org.opendaylight.yangtools.yang.model.api.ChoiceNode> {

    @Override
    protected final DataSchemaNode getSchemaForChild(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> childNode) {
        return SchemaUtils.findSchemaForChild(schema, childNode.getNodeType());
    }

    @Override
    protected final AugmentationSchema getAugmentedCase(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema,
            AugmentationNode augmentationNode) {
        return SchemaUtils.findSchemaForAugment(schema, augmentationNode.getIdentifier().getPossibleChildNames());
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema, AugmentationSchema augmentationSchema) {
        Set<DataSchemaNode> aggregatedSchemas = Sets.newHashSet();

        aggregatedSchemas.addAll(SchemaUtils.getRealSchemasForAugment(schema, augmentationSchema));

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            aggregatedSchemas.addAll(SchemaUtils.getRealSchemasForAugment((AugmentationTarget) choiceCaseNode, augmentationSchema));
        }

        return aggregatedSchemas;
    }
}