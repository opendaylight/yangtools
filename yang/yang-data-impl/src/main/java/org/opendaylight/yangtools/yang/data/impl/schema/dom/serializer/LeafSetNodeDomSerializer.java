/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import com.google.common.collect.Lists;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class LeafSetNodeDomSerializer {

    public List<Element> toDomElement(LeafListSchemaNode schema, LeafSetNode<?> node) {
        return toDomElement(schema, node, DomUtils.defaultValueCodecProvider());
    }

    public List<Element> toDomElement(LeafListSchemaNode schema, LeafSetNode<?> node, XmlCodecProvider codecProvider) {
        Document doc = XmlDocumentUtils.getDocument();
        return toDomElement(schema, node, codecProvider, doc);
    }

    public List<Element> toDomElement(LeafListSchemaNode schema, LeafSetNode<?> node, XmlCodecProvider codecProvider, Document doc) {

        List<Element> elements = Lists.newArrayList();

        for (LeafSetEntryNode<?> leafSetEntryNode : node.getValue()) {
            Element serializedChild = new LeafSetEntryNodeDomSerializer().toDomElement(schema, leafSetEntryNode, codecProvider, doc);
            elements.add(serializedChild);
        }

        return elements;
    }
}
