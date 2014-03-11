/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class LeafSetNodeDomSerializer implements
        DomSerializer<InstanceIdentifier.NodeIdentifier, LeafSetNode<?>, LeafListSchemaNode> {

    @Override
    public List<Element> toDom(LeafListSchemaNode schema, LeafSetNode<?> node, XmlCodecProvider codecProvider,
            Document doc) {

        List<Element> elements = Lists.newArrayList();

        for (LeafSetEntryNode<?> leafSetEntryNode : node.getValue()) {
            List<Element> serializedChild = new LeafSetEntryNodeDomSerializer().toDom(schema, leafSetEntryNode,
                    codecProvider, doc);
            Preconditions.checkState(serializedChild.size() == 1,
                    "Unexpected count of dom elements for leaf-list entry serialized from: %s, should be 1, was: %s",
                    leafSetEntryNode, serializedChild.size());
            elements.addAll(serializedChild);
        }

        return elements;
    }
}
