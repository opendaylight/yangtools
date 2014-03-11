/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ContainerNodeDomSerializer extends
        AbstractDispatcherSerializer<InstanceIdentifier.NodeIdentifier, ContainerNode, ContainerSchemaNode> {

    @Override
    public List<Element> toDom(ContainerSchemaNode schema, ContainerNode containerNode, XmlCodecProvider codec,
            Document doc) {
        Element itemEl = XmlDocumentUtils.createElementFor(doc, containerNode);

        for (Element element : super.toDom(schema, containerNode, codec, doc)) {
            itemEl.appendChild(element);
        }
        return Collections.singletonList(itemEl);
    }

    @Override
    protected DataSchemaNode getSchemaForChild(ContainerSchemaNode schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild) {
        return DomUtils.findSchemaForChild(schema, choiceChild.getNodeType());
    }

    @Override
    protected AugmentationSchema getAugmentedCase(ContainerSchemaNode schema, AugmentationNode choiceChild) {
        return DomUtils.findSchemaForAugment(schema, choiceChild.getIdentifier().getPossibleChildNames());
    }

    @Override
    protected Set<DataSchemaNode> getRealSchemasForAugment(ContainerSchemaNode schema, AugmentationSchema augmentSchema) {
        return DomUtils.getRealSchemasForAugment((AugmentationTarget) schema, augmentSchema);
    }
}
