/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.MapNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class MapNodeCnSnParser extends MapNodeBaseParser<Node<?>> {

    private final MapEntryNodeCnSnParser mapEntryNodeCnSnParser;

    public MapNodeCnSnParser(MapEntryNodeCnSnParser mapEntryNodeCnSnParser) {
        this.mapEntryNodeCnSnParser = mapEntryNodeCnSnParser;
    }

    @Override
    protected ToNormalizedNodeParser<Node<?>, MapEntryNode, ListSchemaNode> getMapEntryNodeParser() {
        return mapEntryNodeCnSnParser;
    }

}
