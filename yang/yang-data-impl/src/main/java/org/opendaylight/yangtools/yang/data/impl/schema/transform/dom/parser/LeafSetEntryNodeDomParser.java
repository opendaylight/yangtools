/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.LeafSetEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

final class LeafSetEntryNodeDomParser extends LeafSetEntryNodeBaseParser<Element> {

    private final XmlCodecProvider codecProvider;
    private final SchemaContext ctx;

    LeafSetEntryNodeDomParser(final XmlCodecProvider codecProvider, final SchemaContext schema) {
        this.ctx = schema;
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    LeafSetEntryNodeDomParser(final XmlCodecProvider codecProvider, final SchemaContext schema, final  BuildingStrategy<YangInstanceIdentifier.NodeWithValue, LeafSetEntryNode<?>> strategy) {
        super(strategy);
        this.ctx = schema;
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    @Deprecated
    public LeafSetEntryNodeDomParser(final XmlCodecProvider codecProvider) {
        this(codecProvider, null);
    }

    @Override
    protected Object parseLeafListEntry(Element xmlElement, LeafListSchemaNode schema) {
        return ctx == null ? DomUtils.parseXmlValue(xmlElement, codecProvider, schema.getType()) : DomUtils.parseXmlValue(xmlElement, codecProvider, schema, schema.getType(), ctx);
    }

    @Override
    protected Map<QName, String> getAttributes(Element element) {
        return DomUtils.toAttributes(element.getAttributes());
    }

}
