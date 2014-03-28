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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class MapEntryNodeModification extends AbstractContainerNodeModification<ListSchemaNode, MapEntryNode> {

    @Override
    protected QName getQName(ListSchemaNode schema) {
        return schema.getQName();
    }

    @Override
    protected Object findSchemaForChild(ListSchemaNode schema, QName nodeType) {
        return SchemaUtils.findSchemaForChild(schema, nodeType);
    }

    @Override
    protected Object findSchemaForAugment(ListSchemaNode schema, InstanceIdentifier.AugmentationIdentifier childToProcessId) {
        return SchemaUtils.findSchemaForAugment(schema, childToProcessId.getPossibleChildNames());
    }

    @Override
    protected DataContainerNodeBuilder<?, MapEntryNode> getBuilder(ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }
}
