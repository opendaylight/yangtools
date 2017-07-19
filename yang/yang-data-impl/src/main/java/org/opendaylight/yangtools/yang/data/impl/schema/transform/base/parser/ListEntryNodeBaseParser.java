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
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Abstract(base) parser for ListNodes (MapNode, UnkeyedListNode), parses elements of type E.
 *
 * @param <E>
 *            type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class ListEntryNodeBaseParser<P extends YangInstanceIdentifier.PathArgument, E, N extends DataContainerNode<P>> extends
        BaseDispatcherParser<E, P, N, ListSchemaNode> {

    public ListEntryNodeBaseParser(final BuildingStrategy<P, N> buildingStrategy) {
        super(buildingStrategy);
    }

    public ListEntryNodeBaseParser() {
    }

    @Override
    protected final Set<DataSchemaNode> getRealSchemasForAugment(final ListSchemaNode schema, final AugmentationSchema augmentSchema) {
        return SchemaUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected final DataSchemaNode getSchemaForChild(final ListSchemaNode schema, final QName childQName) {
        return SchemaUtils.findSchemaForChild(schema, childQName, strictParsing());
    }

    @Override
    protected final Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected final Map<QName, AugmentationSchema> mapChildElementsFromAugments(final ListSchemaNode schema) {
        return SchemaUtils.mapChildElementsFromAugments(schema);
    }

    @Override
    public final N parse(final Iterable<E> elements, final ListSchemaNode schema) {
        checkOnlyOneNode(schema, elements);
        return super.parse(elements, schema);
    }

    @Override
    protected abstract Map<QName, String> getAttributes(E e);
}