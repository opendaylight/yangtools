/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public abstract class LeafSetNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafSetNode<?>, LeafListSchemaNode> {

    protected LeafSetEntryNodeBaseParser<E> leafSetEntryNodeBaseParser = null;

    public LeafSetNodeBaseParser() {
    }

    @Override
    public LeafSetNode<?> parse(List<E> childNodes, LeafListSchemaNode schema) {

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder = Builders.leafSetBuilder(schema);
        for (E childNode : childNodes) {
            LeafSetEntryNode<Object> builtChild = leafSetEntryNodeBaseParser.parse(
                    Collections.singletonList(childNode), schema);
            leafListBuilder.withChild(builtChild);
        }

        return leafListBuilder.build();
    }
}
