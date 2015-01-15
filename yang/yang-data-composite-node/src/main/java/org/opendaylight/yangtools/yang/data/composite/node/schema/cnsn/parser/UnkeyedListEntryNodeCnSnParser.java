/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnkeyedListEntryNodeCnSnParser extends ListEntryNodeCnSnParser<UnkeyedListEntryNode> {

    UnkeyedListEntryNodeCnSnParser(final NodeParserDispatcher<Node<?>> dispatcher) {
        super(dispatcher);
    }

    @Override
    protected final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, UnkeyedListEntryNode> getBuilder(
            ListSchemaNode schema) {
        return Builders.unkeyedListEntryBuilder().withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

}
