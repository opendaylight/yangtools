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
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

@Deprecated
final class MapEntryNodeDomParser extends ListEntryNodeDomParser<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> {

    private final boolean strictParsing;

    MapEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher) {
        super(dispatcher);
        // TODO strict parsing attribute should be injected into superclass via a constructor
        // With the current approach (getter) we have to call super.strictParsing in constructor
        // and cannot reuse constructors
        this.strictParsing = super.strictParsing();
    }

    MapEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher, final boolean strictParsing) {
        super(dispatcher);
        this.strictParsing = strictParsing;
    }

    MapEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher, final BuildingStrategy<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> strategy,
                          final boolean strictParsing) {
        super(strategy, dispatcher);
        this.strictParsing = strictParsing;
    }

    @Override
    protected DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> getBuilder(
            final ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }

    @Override
    protected boolean strictParsing() {
        return strictParsing;
    }
}
