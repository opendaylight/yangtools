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
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafSetEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class LeafSetEntryNodeCnSnParser extends LeafSetEntryNodeBaseParser<Node<?>> {


    public LeafSetEntryNodeCnSnParser() {
        super();
    }

    @Override
    protected Object parseLeafListEntry(List<Node<?>> elements, LeafListSchemaNode schema) {
        return elements.get(0).getValue();
    }

    @Override
    protected Map<QName, String> getAttributes(Node<?> e) {
        return Collections.emptyMap();
    }
}
