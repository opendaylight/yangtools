/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.*;

public abstract class MapEntryNodeBaseParser<E> extends BaseDispatcherParser<E, MapEntryNode, ListSchemaNode> {

    public MapEntryNodeBaseParser() {
        super();
    }

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> getBuilder(
            ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(ListSchemaNode schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(ListSchemaNode schema, QName childPartialQName) {
        return SchemaUtils.findSchemaForChild(schema, childPartialQName);
    }

    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }
}
