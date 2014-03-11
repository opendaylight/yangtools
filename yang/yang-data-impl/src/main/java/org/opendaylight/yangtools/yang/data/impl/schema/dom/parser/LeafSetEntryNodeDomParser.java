/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

public final class LeafSetEntryNodeDomParser implements
        DomParser<LeafSetEntryNode<?>, LeafListSchemaNode> {

    @Override
    public LeafSetEntryNode<Object> fromDom(List<Element> xml, LeafListSchemaNode schema, XmlCodecProvider codecProvider) {
        Preconditions.checkArgument(xml.size() == 1, "Xml elements mapped to leaf node illegal count: %s", xml.size());
        Object value = DomUtils.parseXmlValue(xml.get(0), codecProvider, schema.getType());
        return Builders.leafSetEntryBuilder(schema).withValue(value).build();
    }
}
