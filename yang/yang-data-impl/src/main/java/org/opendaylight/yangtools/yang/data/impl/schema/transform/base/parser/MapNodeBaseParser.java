/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public abstract class MapNodeBaseParser<E> implements ToNormalizedNodeParser<E, MapNode, ListSchemaNode> {

    protected MapEntryNodeBaseParser<E> mapEntryNodeBaseParser = null;

    public MapNodeBaseParser() {
    }

    @Override
    public MapNode parse(List<E> childNodes, ListSchemaNode schema) {
        CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder(schema);

        for (E childNode : childNodes) {
            MapEntryNode listChild = mapEntryNodeBaseParser.parse(Collections.singletonList(childNode), schema);
            listBuilder.withChild(listChild);
        }

        return listBuilder.build();
    }
}
