/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import java.util.Collections;

/**
 * Abstract(base) parser for MapNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class MapNodeBaseParser<E> implements ToNormalizedNodeParser<E, MapNode, ListSchemaNode> {

    @Override
    public final MapNode parse(Iterable<E> childNodes, ListSchemaNode schema) {
        if (schema.isUserOrdered()) {
            CollectionNodeBuilder<MapEntryNode, OrderedMapNode> listBuilder = Builders.orderedMapBuilder(schema);

            for (E childNode : childNodes) {
                MapEntryNode listChild = getMapEntryNodeParser().parse(Collections.singletonList(childNode), schema);
                listBuilder.withChild(listChild);
            }

            return listBuilder.build();
        } else {
            CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder(schema);

            for (E childNode : childNodes) {
                MapEntryNode listChild = getMapEntryNodeParser().parse(Collections.singletonList(childNode), schema);
                listBuilder.withChild(listChild);
            }

            return listBuilder.build();
        }
    }

    /**
     *
     * @return parser for inner MapEntryNodes used to parse every entry of MapNode, might be the same instance in case its immutable
     */
    protected abstract ToNormalizedNodeParser<E, MapEntryNode, ListSchemaNode> getMapEntryNodeParser();

}
