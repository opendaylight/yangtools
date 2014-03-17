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

public abstract class AugmentationNodeBaseSerializer<E> extends
        BaseDispatcherSerializer<E, AugmentationNode, AugmentationSchema> {

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(AugmentationSchema schema, AugmentationSchema childSchema) {
        return SchemaUtils.getRealSchemasForAugment(schema, childSchema);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(AugmentationSchema schema,
                                               DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild) {
        return SchemaUtils.findSchemaForChild(schema, choiceChild.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(AugmentationSchema schema, AugmentationNode choiceChild) {
        throw new UnsupportedOperationException("");
    }

}
