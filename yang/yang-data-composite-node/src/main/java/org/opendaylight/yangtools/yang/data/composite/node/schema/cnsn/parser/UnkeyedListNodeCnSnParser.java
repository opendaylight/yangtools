/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.UnkeyedListNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnkeyedListNodeCnSnParser extends UnkeyedListNodeBaseParser<Node<?>> {

    private final UnkeyedListEntryNodeCnSnParser unkeyedListEntryNodeParser;

    UnkeyedListNodeCnSnParser(UnkeyedListEntryNodeCnSnParser unkeyedListEntryNodeParser) {
        this.unkeyedListEntryNodeParser = unkeyedListEntryNodeParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, UnkeyedListEntryNode, ListSchemaNode> getListEntryNodeParser() {
        return unkeyedListEntryNodeParser;
    }
}
