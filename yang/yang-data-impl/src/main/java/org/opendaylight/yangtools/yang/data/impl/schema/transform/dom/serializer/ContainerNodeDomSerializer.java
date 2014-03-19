/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.ContainerNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ContainerNodeDomSerializer extends
        ContainerNodeBaseSerializer<Element> {

    private final Document doc;
    private final XmlCodecProvider codecProvider;

    public ContainerNodeDomSerializer(Document doc, XmlCodecProvider codecProvider) {
        this.doc = Preconditions.checkNotNull(doc);
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    @Override
    public List<Element> serialize(ContainerSchemaNode schema, ContainerNode containerNode) {
        Element itemEl = XmlDocumentUtils.createElementFor(doc, containerNode);

        for (Element element : super.serialize(schema, containerNode)) {
            itemEl.appendChild(element);
        }
        return Collections.singletonList(itemEl);
    }

    @Override
    protected NodeSerializerDispatcher<Element> getNodeDispatcher() {
        return DomNodeSerializerDispatcher.getInstance(doc, codecProvider);
    }

}
