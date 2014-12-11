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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Abstract(base) parser for MapEntryNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class MapEntryNodeBaseParser<E> extends BaseDispatcherParser<E, MapEntryNode, ListSchemaNode> {

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> getBuilder(
            ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }

    @Override
    public final MapEntryNode parse(Iterable<E> elements, ListSchemaNode schema) {
        checkOnlyOneNode(schema, elements);
        return super.parse(elements, schema);
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(ListSchemaNode schema, AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(ListSchemaNode schema, QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName);
    }

    @Override
    protected final Map<QName, ChoiceNode> mapChildElementsFromChoices(ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }

    @Override
    protected abstract Map<QName, String> getAttributes(E e);
}
