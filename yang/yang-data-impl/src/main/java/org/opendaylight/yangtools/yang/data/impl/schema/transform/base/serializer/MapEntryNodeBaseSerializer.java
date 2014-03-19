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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public abstract class MapEntryNodeBaseSerializer<E> extends
        BaseDispatcherSerializer<E, MapEntryNode, ListSchemaNode> {

    @Override
    protected DataSchemaNode getSchemaForChild(ListSchemaNode schema,
                                               DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild) {
        return SchemaUtils.findSchemaForChild(schema, choiceChild.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(ListSchemaNode schema, AugmentationNode choiceChild) {
        return SchemaUtils.findSchemaForAugment(schema, choiceChild.getIdentifier().getPossibleChildNames());
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(ListSchemaNode schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

}
