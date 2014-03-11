/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.MapEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.json.schema.json.JsonCnSnUtils;

import com.google.common.collect.LinkedListMultimap;

public final class MapEntryNodeCnSnParser extends MapEntryNodeBaseParser<Node<?>> {


    public MapEntryNodeCnSnParser() {
        super();
    }


    @Override
    protected LinkedListMultimap<QName, Node<?>> mapChildElements(List<Node<?>> elements) {
        return JsonCnSnUtils.mapChildElementsForSingletonNode(elements.iterator().next());
    }

    @Override
    protected NodeParserDispatcher<Node<?>> getDispatcher() {
        return CnSnNodeDispatcher.getInstance();
    }


}
