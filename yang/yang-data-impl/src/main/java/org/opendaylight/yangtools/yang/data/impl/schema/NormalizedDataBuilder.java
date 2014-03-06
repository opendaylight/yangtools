/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Collection;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * XML <-> NormalizedNode
 */
public class NormalizedDataBuilder {

    private static XmlCodecProvider getCodecProvider() {
        return XmlDocumentUtils.defaultValueCodecProvider();
    }

    // TODO refactor and finish

    public static ContainerNode buildFromDomElement(Element xml, ContainerSchemaNode schema,
            XmlCodecProvider codecProvider) {
        NodeList childNodes = xml.getChildNodes();

        Multimap<QName, Element> mappedChildElements = mapChildElments(childNodes);

        Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> builtChildNodes = Maps.newHashMap();

        for (QName childPartialQName : mappedChildElements.keySet()) {
            Collection<Element> childrenForQName = mappedChildElements.get(childPartialQName);

            Optional<DataSchemaNode> childSchema = XmlDocumentUtils.findFirstSchema(childPartialQName,
                    schema.getChildNodes());
            Preconditions.checkState(childSchema.isPresent(),
                    "Unknown child(ren) node(s) detected, identified by: %s, found: %s", childPartialQName,
                    childrenForQName);

            DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> builtChildNode = dispatchChildElement(
                    childSchema.get(), childrenForQName, codecProvider);
            builtChildNodes.put(builtChildNode.getIdentifier(), builtChildNode);
        }

        return new ImmutableContainerNode(new InstanceIdentifier.NodeIdentifier(schema.getQName()), builtChildNodes);
    }

    private static DataContainerChild<?, ?> dispatchChildElement(DataSchemaNode schema, Collection<Element> childNodes, XmlCodecProvider codecProvider) {
        if (schema instanceof ContainerSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Container node detected multiple times, identified by: %s, found: %s", schema.getQName(),
                    childNodes);
            return buildFromDomElement(childNodes.iterator().next(), (ContainerSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafSchemaNode) {
            Preconditions.checkArgument(childNodes.size() == 1,
                    "Container node detected multiple times, identified by: %s, found: %s", schema.getQName(),
                    childNodes);
            return buildFromDomElement(childNodes.iterator().next(), (LeafSchemaNode) schema, codecProvider);
        } else if (schema instanceof LeafListSchemaNode) {
            return buildFromDomElements(childNodes, (LeafListSchemaNode) schema, codecProvider);
        } else if (schema instanceof ListSchemaNode) {
            return buildFromDomElements(childNodes, (ListSchemaNode) schema, codecProvider);
        }

        throw new IllegalArgumentException("Unable to build from " + schema);
    }

    private static DataContainerChild<?, ?> buildFromDomElements(Collection<Element> childNodes, ListSchemaNode schema, XmlCodecProvider codecProvider) {
        return null;
    }

    private static LeafSetNode<?> buildFromDomElements(Collection<Element> childNodes, LeafListSchemaNode schema,
            XmlCodecProvider codecProvider) {
        Map<Object, LeafSetEntryNode<Object>> builtChildren = Maps.newLinkedHashMap();

        for (Element childNode : childNodes) {
            LeafSetEntryNode<?> builtChild = buildFromDomElement(childNode, schema, codecProvider);
            // FIXME, generic type mismatch
            builtChildren.put(builtChild.getValue(), (LeafSetEntryNode<Object>)builtChild);
        }

//        return new ImmutableLeafSetNode<>(new InstanceIdentifier.NodeIdentifier(schema.getQName()), builtChildren);
        return null;
    }

    private static LeafNode<?> buildFromDomElement(Element xml,
            LeafSchemaNode schema, XmlCodecProvider codecProvider) {
        Object value = buildFromDomElement(xml, codecProvider, schema.getType());
        return new ImmutableLeafNode<>(new InstanceIdentifier.NodeIdentifier(schema.getQName()), value);
    }

    private static Object buildFromDomElement(Element xml, XmlCodecProvider codecProvider,
                                              TypeDefinition<?> type) {
        TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = codecProvider.codecFor(type);
        String text = xml.getTextContent();
        Object value;
        if (codecProvider != null) {
            value = codec.deserialize(text);

        } else {
            value = xml.getTextContent();
        }

        return value;
    }

    private static LeafSetEntryNode<?> buildFromDomElement(Element xml,
            LeafListSchemaNode schema, XmlCodecProvider codecProvider) {
        Object value = buildFromDomElement(xml, codecProvider, schema.getType());
        return new ImmutableLeafSetEntryNode<>(new InstanceIdentifier.NodeWithValue(schema.getQName(), value), value);
    }

    private static Multimap<QName, Element> mapChildElments(NodeList childNodes) {
        Multimap<QName, Element> mappedChildElements = LinkedHashMultimap.create();

        for (int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i) instanceof Element == false) {
                continue;
            }
            Element childElement = (Element) childNodes.item(i);
            QName childQName = XmlDocumentUtils.qNameFromElement(childElement);
            mappedChildElements.put(childQName, childElement);
        }
        return mappedChildElements;
    }
}
