/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LeafSetEntryNodeDomSerializer {

    public Element toDomElement(LeafListSchemaNode schema, LeafSetEntryNode<?> node) {
        return toDomElement(schema, node, DomUtils.defaultValueCodecProvider());
    }

    public Element toDomElement(LeafListSchemaNode schema, LeafSetEntryNode<?> node, XmlCodecProvider codecProvider) {
        Document doc = XmlDocumentUtils.getDocument();
        return toDomElement(schema, node, codecProvider, doc);
    }

    public Element toDomElement(LeafListSchemaNode schema, LeafSetEntryNode<?> node, XmlCodecProvider codecProvider, Document doc) {
        Element itemEl = XmlDocumentUtils.createElementFor(doc, node);

        DomUtils.serializeXmlValue(itemEl, schema.getType(), codecProvider, node.getValue());

        return itemEl;
    }
}
