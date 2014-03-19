/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

class ContainerNodeModification extends AbstractContainerNodeModification<ContainerSchemaNode, ContainerNode> {

    @Override
    protected QName getQName(ContainerSchemaNode schema) {
        return schema.getQName();
    }

    @Override
    protected Object findSchemaForChild(ContainerSchemaNode schema, QName nodeType) {
        return SchemaUtils.findSchemaForChild(schema, nodeType);
    }

    @Override
    protected Object findSchemaForAugment(ContainerSchemaNode schema,
            InstanceIdentifier.AugmentationIdentifier childToProcessId) {
        return SchemaUtils.findSchemaForAugment(schema, childToProcessId.getPossibleChildNames());
    }

    @Override
    protected DataContainerNodeBuilder<?, ContainerNode> getBuilder(ContainerSchemaNode schema) {
        return Builders.containerBuilder(schema);
    }

}
