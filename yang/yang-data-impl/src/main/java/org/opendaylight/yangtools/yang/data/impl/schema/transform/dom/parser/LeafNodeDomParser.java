/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Element;

public class LeafNodeDomParser extends LeafNodeBaseParser<Element> {

    private final XmlCodecProvider codecProvider;

    public LeafNodeDomParser(XmlCodecProvider codecProvider) {
        super();
        this.codecProvider = codecProvider;
    }

    @Override
    protected Object parseLeaf(List<Element> xmlElement, LeafSchemaNode schema) {
        return DomUtils.parseXmlValue(xmlElement.get(0), codecProvider, schema.getType());
    }

    @Override
    protected Map<QName, String> getAttributes(Element element) {
        return DomUtils.toAttributes(element.getAttributes());
    }
}
