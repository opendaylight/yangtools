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
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ParsingStrategy;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

final class MapEntryNodeDomParser extends ListEntryNodeDomParser<MapEntryNode> {

    private final ParsingStrategy strategy;

    MapEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher) {
        super(dispatcher);
        this.strategy = super.getParsingStrategy();
    }

    MapEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher, final ParsingStrategy strategy) {
        super(dispatcher);
        this.strategy = strategy;
    }

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> getBuilder(
            ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }

    @Override
    protected ParsingStrategy<MapEntryNode> getParsingStrategy() {
        return this.strategy;
    }
}
