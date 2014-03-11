/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

public final class MapNodeDomParser implements DomParser<MapNode, ListSchemaNode> {

    static final MapEntryNodeDomParser MAP_ENTRY_NODE_DOM_PARSER = new MapEntryNodeDomParser();

    @Override
    public MapNode fromDom(List<Element> childNodes, ListSchemaNode schema, XmlCodecProvider codecProvider) {
        CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder(schema);

        for (Element childNode : childNodes) {
            MapEntryNode listChild = MAP_ENTRY_NODE_DOM_PARSER.fromDom(Collections.singletonList(childNode), schema,
                    codecProvider);
            listBuilder.withChild(listChild);
        }

        return listBuilder.build();
    }
}
