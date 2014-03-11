/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Element;

public class LeafSetNodeDomParser implements
        DomParser<InstanceIdentifier.NodeIdentifier, LeafSetNode<?>, LeafListSchemaNode> {

    private static final LeafSetEntryNodeDomParser LEAF_SET_ENTRY_NODE_DOM_PARSER = new LeafSetEntryNodeDomParser();

    @Override
    public LeafSetNode<?> fromDom(List<Element> childNodes, LeafListSchemaNode schema, XmlCodecProvider codecProvider) {

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder = Builders.leafSetBuilder(schema);
        for (Element childNode : childNodes) {
            LeafSetEntryNode<Object> builtChild = LEAF_SET_ENTRY_NODE_DOM_PARSER.fromDom(
                    Collections.singletonList(childNode), schema, codecProvider);
            leafListBuilder.withChild(builtChild);
        }

        return leafListBuilder.build();
    }

}
