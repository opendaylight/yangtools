/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;

import java.util.Collection;

public class MapEntryNodeDomParser {

    public MapEntryNode fromDomElement(Element xml, ListSchemaNode schema,
                                              XmlCodecProvider codecProvider) {

        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> containerBuilder = Builders.mapEntryBuilder(schema);

        Multimap<QName, Element> mappedChildElements = DomUtils.mapChildElements(xml.getChildNodes());

        for (QName childPartialQName : mappedChildElements.keySet()) {
            Collection<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            DataSchemaNode childSchema = DomUtils.findSchemaForChild(schema, childPartialQName);

            // TODO refactor dispatch
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = ContainerNodeDomParser.dispatchChildElement(
                    childSchema, childrenForQName, codecProvider);
            containerBuilder.withChild(builtChildNode);
        }

        return containerBuilder.build();
    }

    public MapEntryNode fromDomElement(Element xml, ListSchemaNode schema) {
        return fromDomElement(xml, schema, DomUtils.defaultValueCodecProvider());
    }
}
