/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

final class UnkeyedListEntryNodeDomParser extends ListEntryNodeDomParser<YangInstanceIdentifier.NodeIdentifier, UnkeyedListEntryNode> {
    private final boolean strictParsing;

    UnkeyedListEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher, final boolean strictParsing) {
        super(dispatcher);
        this.strictParsing = strictParsing;
    }

    UnkeyedListEntryNodeDomParser(final BuildingStrategy<NodeIdentifier, UnkeyedListEntryNode> buildingStrategy,
            final NodeParserDispatcher<Element> dispatcher, final boolean strictParsing) {
        super(buildingStrategy, dispatcher);
        this.strictParsing = strictParsing;
    }

    @Override
    protected DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, UnkeyedListEntryNode> getBuilder(
            final ListSchemaNode schema) {
        return Builders.unkeyedListEntryBuilder().withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    @Override
    protected boolean strictParsing() {
        return strictParsing;
    }
}
