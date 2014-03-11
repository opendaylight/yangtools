/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;

import com.google.common.collect.LinkedListMultimap;

public final class MapEntryNodeDomParser extends
        AbstractDispatcherParser<MapEntryNode, ListSchemaNode> {

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> getBuilder(
            ListSchemaNode schema) {
        return Builders.mapEntryBuilder(schema);
    }

    @Override
    public MapEntryNode fromDom(List<Element> xml, ListSchemaNode schema, XmlCodecProvider codecProvider) {
        checkOnlyOneNode(schema, xml);
        return super.fromDom(xml, schema, codecProvider);
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(ListSchemaNode schema, AugmentationSchema augmentSchema) {
        return DomUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(List<Element> xml) {
        return DomUtils.mapChildElementsForSingletonNode(xml.iterator().next());
    }

    @Override
    protected DataSchemaNode getSchemaForChild(ListSchemaNode schema, QName childPartialQName) {
        return DomUtils.findSchemaForChild(schema, childPartialQName);
    }

    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(ListSchemaNode schema) {
        return DomUtils.mapChildElementsFromChoices(schema);
    }

    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(ListSchemaNode schema) {
        return DomUtils.mapChildElementsFromAugments(schema);
    }
}
