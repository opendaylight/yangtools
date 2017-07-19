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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract(base) parser for ContainerNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class ContainerNodeBaseParser<E> extends
        BaseDispatcherParser<E, YangInstanceIdentifier.NodeIdentifier, ContainerNode, ContainerSchemaNode> {

    public ContainerNodeBaseParser() {}

    public ContainerNodeBaseParser(final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, ContainerNode> buildingStrategy) {
        super(buildingStrategy);
    }

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ContainerNode> getBuilder(
            final ContainerSchemaNode schema) {
        return Builders.containerBuilder(schema);
    }

    @Override
    public final ContainerNode parse(final Iterable<E> elements, final ContainerSchemaNode schema) {
        checkOnlyOneNode(schema, elements);
        return super.parse(elements, schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(final ContainerSchemaNode schema, final AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(final ContainerSchemaNode schema, final QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName, strictParsing());
    }

    @Override
    protected final Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final ContainerSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(final ContainerSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }

    @Override
    protected abstract Map<QName, String> getAttributes(E e);

}
