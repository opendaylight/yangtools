/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ContainerNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.json.schema.json.CnSnToNormalizedNodesUtils;

import com.google.common.collect.LinkedListMultimap;

public final class ContainerNodeCnSnParser extends ContainerNodeBaseParser<Node<?>> {

    public ContainerNodeCnSnParser() {
        super();
    }


    @Override
    protected NodeParserDispatcher<Node<?>> getDispatcher() {
        return CnSnNodeDispatcher.getInstance();
    }

    @Override
    protected LinkedListMultimap<QName, Node<?>> mapChildElements(List<Node<?>> elements) {
        return CnSnToNormalizedNodesUtils.mapChildElementsForSingletonNode(elements.iterator().next());
    }


    @Override
    protected Map<QName, String> getAttributes(Node<?> e) {
        return Collections.emptyMap();
    }

}
