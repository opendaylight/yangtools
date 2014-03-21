/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.AugmentationNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.json.schema.json.CnSnToNormalizedNodesUtils;

import com.google.common.collect.LinkedListMultimap;

public final class AugmentationNodeCnSnParser extends AugmentationNodeBaseParser<Node<?>> {

    public AugmentationNodeCnSnParser() {
        super();
    }

    @Override
    protected LinkedListMultimap<QName, Node<?>> mapChildElements(Iterable<Node<?>> elements) {
        return CnSnToNormalizedNodesUtils.mapChildElements(elements);
    }

    @Override
    protected NodeParserDispatcher<Node<?>> getDispatcher() {
        return CnSnNodeDispatcher.getInstance();
    }

}
