/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class ContainerNodeDomSerializer {

    // TODO pull up common abstract serializer

    public Element toDomElement(ContainerSchemaNode schema, ContainerNode containerNode) {
        return toDomElement(schema, containerNode, DomUtils.defaultValueCodecProvider());
    }

    public Element toDomElement(ContainerSchemaNode schema, ContainerNode containerNode, XmlCodecProvider codecProvider) {
        Document doc = XmlDocumentUtils.getDocument();
        return toDomElement(schema, containerNode, codecProvider, doc);
    }

    public Element toDomElement(ContainerSchemaNode schema, ContainerNode containerNode,
            XmlCodecProvider codecProvider, Document doc) {
        Element itemEl = XmlDocumentUtils.createElementFor(doc, containerNode);

        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild : containerNode
                .getValue()) {

            if(dataContainerChild instanceof AugmentationNode) {

                AugmentationSchema childSchema = getAugmentedCase(schema, (AugmentationNode) dataContainerChild);
                List<Element> childElements = new AugmentationNodeDomSerializer().toDomElement(childSchema, (AugmentationNode) dataContainerChild, codecProvider, doc);
                for (Element childElement : childElements) {
                    itemEl.appendChild(childElement);
                }
            } else {
                DataSchemaNode childSchema =DomUtils.findSchemaForChild(schema, dataContainerChild.getNodeType());
                List<Element> childElements = dispatchChildElement(childSchema, dataContainerChild, codecProvider, doc);
                for (Element childElement : childElements) {
                    itemEl.appendChild(childElement);
                }
            }
        }
        return itemEl;
    }

    private AugmentationSchema getAugmentedCase(AugmentationTarget schema, AugmentationNode choiceChild) {
        return DomUtils.findSchemaForAugment(schema, choiceChild.getIdentifier().getPossibleChildNames());
    }

    // TODO refactor
    static List<Element> dispatchChildElement(DataSchemaNode childSchema,
            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild,
            XmlCodecProvider codecProvider, Document doc) {
        if (dataContainerChild instanceof ContainerNode) {
            Preconditions.checkArgument(childSchema instanceof ContainerSchemaNode,
                    "Incompatible schema: %s with node: %S", childSchema, dataContainerChild);
            return Lists.newArrayList(new ContainerNodeDomSerializer().toDomElement((ContainerSchemaNode) childSchema,
                    (ContainerNode) dataContainerChild, codecProvider, doc));
        } else if (dataContainerChild instanceof LeafNode<?>) {
            Preconditions.checkArgument(childSchema instanceof LeafSchemaNode, "Incompatible schema: %s with node: %S",
                    childSchema, dataContainerChild);
            return Lists.newArrayList(new LeafNodeDomSerializer().toDomElement((LeafSchemaNode) childSchema,
                    (LeafNode<?>) dataContainerChild, codecProvider, doc));
        } else if (dataContainerChild instanceof MixinNode) {
            if (dataContainerChild instanceof LeafSetNode<?>) {
                Preconditions.checkArgument(childSchema instanceof LeafListSchemaNode,
                        "Incompatible schema: %s with node: %S", childSchema, dataContainerChild);
                return new LeafSetNodeDomSerializer().toDomElement((LeafListSchemaNode) childSchema,
                        (LeafSetNode<?>) dataContainerChild, codecProvider, doc);
            } else if (dataContainerChild instanceof MapNode) {
                Preconditions.checkArgument(childSchema instanceof ListSchemaNode,
                        "Incompatible schema: %s with node: %S", childSchema, dataContainerChild);
                return new MapNodeDomSerializer().toDomElement((ListSchemaNode) childSchema,
                        (MapNode) dataContainerChild, codecProvider, doc);
            } else if (dataContainerChild instanceof ChoiceNode) {
                Preconditions.checkArgument(
                        childSchema instanceof org.opendaylight.yangtools.yang.model.api.ChoiceNode,
                        "Incompatible schema: %s with node: %S", childSchema, dataContainerChild);
                return new ChoiceNodeDomSerializer().toDomElement(
                        (org.opendaylight.yangtools.yang.model.api.ChoiceNode) childSchema,
                        (ChoiceNode) dataContainerChild, codecProvider, doc);
            }
        }
        throw new IllegalArgumentException("Unable to serialize " + childSchema);
    }
}
