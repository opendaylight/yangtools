/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Element;

import java.util.Collection;

public class LeafSetNodeDomParser {

    public LeafSetNode<?> fromDomElements(Collection<Element> childNodes, LeafListSchemaNode schema,
                                                 XmlCodecProvider codecProvider) {

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder = Builders.leafSetBuilder(schema);
        for (Element childNode : childNodes) {
            LeafSetEntryNode<Object> builtChild = new LeafSetEntryNodeDomParser().fromDomElement(childNode, schema, codecProvider);
            leafListBuilder.withChild(builtChild);
        }

        return leafListBuilder.build();
    }

    public LeafSetNode<?> fromDomElements(Collection<Element> childNodes, LeafListSchemaNode schema) {
        return fromDomElements(childNodes, schema, DomUtils.defaultValueCodecProvider());
    }
}
