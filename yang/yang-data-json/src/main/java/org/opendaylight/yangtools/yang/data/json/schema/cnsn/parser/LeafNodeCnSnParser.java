/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.parser;

import java.util.Collections;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public class LeafNodeCnSnParser extends LeafNodeBaseParser<Node<?>> {

    public LeafNodeCnSnParser() {
        super();
    }

    @Override
    protected Object parseLeaf(Node<?> elements, LeafSchemaNode schema) {
        return elements.getValue();
    }

    @Override
    protected Map<QName, String> getAttributes(Node<?> e) {
        return Collections.emptyMap();
    }
}
