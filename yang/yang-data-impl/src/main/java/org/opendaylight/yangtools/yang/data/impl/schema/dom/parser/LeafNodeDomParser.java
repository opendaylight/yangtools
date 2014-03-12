/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.AttributesBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Element;

import java.util.List;

public final class LeafNodeDomParser implements
        DomParser<InstanceIdentifier.NodeIdentifier, LeafNode<?>, LeafSchemaNode> {

    @Override
    public LeafNode<?> fromDom(List<Element> xml, LeafSchemaNode schema, XmlCodecProvider codecProvider) {
        Preconditions.checkArgument(xml.size() == 1, "Xml elements mapped to leaf node illegal count: %s", xml.size());
        Object value = DomUtils.parseXmlValue(xml.get(0), codecProvider, schema.getType());
        ImmutableLeafNodeBuilder builder = (ImmutableLeafNodeBuilder) Builders.leafBuilder(schema);
        builder.withValue(value);
        if(builder instanceof AttributesBuilder){
            builder.withAttributes(DomUtils.toAttributes(xml.get(0).getAttributes()));
        }

        return builder.build();
    }
}
