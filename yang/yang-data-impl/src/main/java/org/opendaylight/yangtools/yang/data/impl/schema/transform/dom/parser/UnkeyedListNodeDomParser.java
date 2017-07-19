/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.UnkeyedListNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

@Deprecated
final class UnkeyedListNodeDomParser extends UnkeyedListNodeBaseParser<Element> {

    private final UnkeyedListEntryNodeDomParser unkeyedListEntryNodeParser;

    UnkeyedListNodeDomParser(final UnkeyedListEntryNodeDomParser unkeyedListEntryNodeParser) {
        this.unkeyedListEntryNodeParser = unkeyedListEntryNodeParser;
    }

    UnkeyedListNodeDomParser(final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, UnkeyedListNode> buildingStrategy, final UnkeyedListEntryNodeDomParser unkeyedListEntryNodeParser) {
        super(buildingStrategy);
        this.unkeyedListEntryNodeParser = unkeyedListEntryNodeParser;
    }

    @Override
    protected ToNormalizedNodeParser<Element, UnkeyedListEntryNode, ListSchemaNode> getListEntryNodeParser() {
        return unkeyedListEntryNodeParser;
    }

}
