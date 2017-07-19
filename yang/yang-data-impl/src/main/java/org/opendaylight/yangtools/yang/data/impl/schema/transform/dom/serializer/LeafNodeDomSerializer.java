/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.LeafNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Deprecated
final class LeafNodeDomSerializer extends LeafNodeBaseSerializer<Element> {
    private final XmlCodecProvider codecProvider;
    private final Document doc;

    LeafNodeDomSerializer(final Document doc, final XmlCodecProvider codecProvider) {
        this.doc = Preconditions.checkNotNull(doc);
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    @Override
    protected Element serializeLeaf(final LeafSchemaNode schema, final LeafNode<?> node) {
        Element itemEl = XmlDocumentUtils.createElementFor(doc, node);
        DomUtils.serializeXmlValue(itemEl, schema.getType(), codecProvider, node.getValue());

        return itemEl;
    }
}
