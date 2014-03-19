/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.*;

public abstract class ContainerNodeBaseParser<E> extends
        BaseDispatcherParser<E, ContainerNode, ContainerSchemaNode> {

    public ContainerNodeBaseParser() {
        super();
    }

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> getBuilder(
            ContainerSchemaNode schema) {
        return Builders.containerBuilder(schema);
    }

    @Override
    public ContainerNode parse(List<E> elements, ContainerSchemaNode schema) {
        checkOnlyOneNode(schema, elements);
        return super.parse(elements, schema);
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(ContainerSchemaNode schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(ContainerSchemaNode schema, QName childPartialQName) {
        return SchemaUtils.findSchemaForChild(schema, childPartialQName);
    }

    @Override
    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(ContainerSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(ContainerSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }

}
