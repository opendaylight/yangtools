/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.ChoiceNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChoiceNodeDomSerializer
        extends
        ChoiceNodeBaseSerializer<Element> {

    private final XmlCodecProvider codec;
    private final Document doc;

    public ChoiceNodeDomSerializer(Document doc, XmlCodecProvider codec) {
        this.doc = doc;
        this.codec = codec;
    }

    @Override
    protected NodeSerializerDispatcher<Element> getNodeDispatcher() {
        return DomNodeSerializerDispatcher.getInstance(doc, codec);
    }
}
