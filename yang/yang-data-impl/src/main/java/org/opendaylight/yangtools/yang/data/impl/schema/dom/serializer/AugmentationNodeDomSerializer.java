/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import com.google.common.collect.Lists;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Set;

public class AugmentationNodeDomSerializer {

    public List<Element> toDom(AugmentationSchema schema, AugmentationNode node, Set<DataSchemaNode> realChildSchemas, XmlCodecProvider codec, Document doc) {
        List<Element> choiceChildren = Lists.newArrayList();

        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild : node.getValue()) {

                DataSchemaNode childSchema = DomUtils.findSchemaForChild(null, choiceChild.getNodeType(), realChildSchemas);
                List<Element> childElements = AbstractDispatcherSerializer.NodeDispatcher.dispatchChildElement(childSchema, choiceChild, codec, doc);
                for (Element childElement : childElements) {
                    choiceChildren.add(childElement);
                }

        }

        return choiceChildren;
    }

    protected DataSchemaNode getSchemaForChild(AugmentationSchema schema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> choiceChild) {
        return DomUtils.findSchemaForChild(schema, choiceChild.getNodeType());
    }

    protected AugmentationSchema getAugmentedCase(AugmentationSchema schema, AugmentationNode choiceChild) {
        throw new IllegalStateException(String.format(
                "Augment child nodes are not permitted in augments, parent augment: %s, child augment: %s", schema,
                choiceChild));
    }
}
