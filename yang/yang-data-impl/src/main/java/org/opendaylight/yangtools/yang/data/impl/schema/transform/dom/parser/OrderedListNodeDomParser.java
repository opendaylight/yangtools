/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.OrderedListNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

@Deprecated
final class OrderedListNodeDomParser extends OrderedListNodeBaseParser<Element> {

    private final MapEntryNodeDomParser mapEntryNodeParser;

    OrderedListNodeDomParser(final MapEntryNodeDomParser mapEntryNodeParser) {
        this.mapEntryNodeParser = mapEntryNodeParser;
    }

    OrderedListNodeDomParser(final MapEntryNodeDomParser mapEntryNodeParser, final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, OrderedMapNode> strategy) {
        super(strategy);
        this.mapEntryNodeParser = mapEntryNodeParser;
    }

    @Override
    protected ToNormalizedNodeParser<Element, MapEntryNode, ListSchemaNode> getListEntryNodeParser() {
        return mapEntryNodeParser;
    }

}
