/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.data.composite.node.schema.json.CnSnToNormalizedNodesUtils;

import java.util.Collections;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.MapEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

public final class MapEntryNodeCnSnParser extends MapEntryNodeBaseParser<Node<?>> {

    private final NodeParserDispatcher<Node<?>> dispatcher;

    public MapEntryNodeCnSnParser(final NodeParserDispatcher<Node<?>> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }


    @Override
    protected LinkedListMultimap<QName, Node<?>> mapChildElements(Iterable<Node<?>> elements) {
        return CnSnToNormalizedNodesUtils.mapChildElementsForSingletonNode(elements.iterator().next());
    }

    @Override
    protected NodeParserDispatcher<Node<?>> getDispatcher() {
        return dispatcher;
    }

    @Override
    protected Map<QName, String> getAttributes(Node<?> e) {
        return Collections.emptyMap();
    }
}
