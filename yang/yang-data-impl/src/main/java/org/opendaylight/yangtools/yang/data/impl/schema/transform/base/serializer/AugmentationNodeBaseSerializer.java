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
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) serializer for AugmentationNode, serializes elements of type E.
 *
 * @param <E> type of serialized elements
 */
public abstract class AugmentationNodeBaseSerializer<E> extends
        BaseDispatcherSerializer<E, AugmentationNode, AugmentationSchema> {

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(AugmentationSchema schema, AugmentationSchema augmentationSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, augmentationSchema);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(AugmentationSchema schema,
                                               DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> childNode) {
        return SchemaUtils.findSchemaForChild(schema, childNode.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(AugmentationSchema schema, AugmentationNode augmentationNode) {
        throw new UnsupportedOperationException("");
    }

}
