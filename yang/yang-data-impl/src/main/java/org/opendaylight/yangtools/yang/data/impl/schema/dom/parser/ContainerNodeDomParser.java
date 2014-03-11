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

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

import com.google.common.collect.LinkedListMultimap;

public final class ContainerNodeDomParser extends
        AbstractDispatcherParser<InstanceIdentifier.NodeIdentifier, ContainerNode, ContainerSchemaNode> {

    @Override
    protected DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> getBuilder(
            ContainerSchemaNode schema) {
        return Builders.containerBuilder(schema);
    }

    public ContainerNode fromDom(List<Element> xml, ContainerSchemaNode schema, XmlCodecProvider codecProvider) {
        checkOnlyOneNode(schema, xml);
        return super.fromDom(xml, schema, codecProvider);
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(List<Element> xml) {
        return DomUtils.mapChildElementsForSingletonNode(xml.iterator().next());
    }

    @Override
    protected DataSchemaNode getSchemaForChild(ContainerSchemaNode schema, QName childPartialQName) {
        return DomUtils.findSchemaForChild(schema, childPartialQName);
    }

    @Override
    protected Map<QName, ChoiceNode> mapChildElementsFromChoices(ContainerSchemaNode schema) {
        return DomUtils.mapChildElementsFromChoices(schema);
    }

    @Override
    protected Map<QName, AugmentationSchema> mapChildElementsFromAugments(ContainerSchemaNode schema) {
        return DomUtils.mapChildElementsFromAugments(schema);
    }

}
