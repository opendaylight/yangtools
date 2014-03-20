/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import java.util.Map;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafSetEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Element;

public class LeafSetEntryNodeDomParser extends LeafSetEntryNodeBaseParser<Element> {

    private final XmlCodecProvider codecProvider;

    public LeafSetEntryNodeDomParser(XmlCodecProvider codecProvider) {
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    @Override
    protected Object parseLeafListEntry(Element xmlElement, LeafListSchemaNode schema) {
        return DomUtils.parseXmlValue(xmlElement, codecProvider, schema.getType());
    }

    @Override
    protected Map<QName, String> getAttributes(Element element) {
        return DomUtils.toAttributes(element.getAttributes());
    }
}
