/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.dom;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DomUtils {
    private static final XmlCodecProvider DEFAULT_XML_VALUE_CODEC_PROVIDER = new XmlCodecProvider() {

        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    public static XmlCodecProvider defaultValueCodecProvider() {
        return DEFAULT_XML_VALUE_CODEC_PROVIDER;
    }

    public static Object parseXmlValue(Element xml, XmlCodecProvider codecProvider, TypeDefinition<?> type) {
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

    public static void serializeXmlValue(Element itemEl, TypeDefinition<? extends TypeDefinition<?>> type, XmlCodecProvider codecProvider, Object value) {
        XmlDocumentUtils.writeValueByType(itemEl, type, codecProvider, value);
    }

    public static DataSchemaNode findSchemaForChild(DataNodeContainer schema, QName qname) {
        Set<DataSchemaNode> childNodes = schema.getChildNodes();
        Optional<DataSchemaNode> childSchema = XmlDocumentUtils.findFirstSchema(qname, childNodes);
        Preconditions.checkState(childSchema.isPresent(),
                "Unknown child(ren) node(s) detected, identified by: %s, in: %s", qname, schema);
        return childSchema.get();
    }

    public static AugmentationSchema findSchemaForAugment(AugmentationTarget schema, Set<QName> qNames) {
        Optional<AugmentationSchema> schemaForAugment = findAugment(schema, qNames);
        Preconditions.checkState(schemaForAugment.isPresent(), "Unknown augmentation node detected, identified by: %s, in: %s",
                qNames, schema);
        return schemaForAugment.get();
    }

    public static AugmentationSchema findSchemaForAugment(ChoiceNode schema, Set<QName> qNames) {
        Optional<AugmentationSchema> schemaForAugment = Optional.absent();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            schemaForAugment = findAugment(choiceCaseNode, qNames);
            if(schemaForAugment.isPresent()) {
                break;
            }
        }

        Preconditions.checkState(schemaForAugment.isPresent(), "Unknown augmentation node detected, identified by: %s, in: %s",
                qNames, schema);
        return schemaForAugment.get();
    }

    private static Optional<AugmentationSchema> findAugment(AugmentationTarget schema, Set<QName> qNames) {
        for (AugmentationSchema augment : schema.getAvailableAugmentations()) {

            HashSet<QName> qNamesFromAugment = Sets.newHashSet(Collections2.transform(augment.getChildNodes(), new Function<DataSchemaNode, QName>() {
                @Override
                public QName apply(DataSchemaNode input) {
                    return input.getQName();
                }
            }));

            if(qNamesFromAugment.equals(qNames)) {
                return Optional.of(augment);
            }
        }

        return Optional.absent();
    }

    public static DataSchemaNode findSchemaForChild(ChoiceNode schema, QName childPartialQName) {
        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            Optional<DataSchemaNode> childSchema = XmlDocumentUtils.findFirstSchema(childPartialQName,
                    choiceCaseNode.getChildNodes());
            if (childSchema.isPresent()) {
                return childSchema.get();
            }
        }
        throw new IllegalStateException(String.format("Unknown child(ren) node(s) detected, identified by: %s, in: %s",
                childPartialQName, schema));
    }

    public static LinkedListMultimap<QName, Element> mapChildElementsForSingletonNode(Element node) {
        List<Element> childNodesCollection = Lists.newArrayList();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i) instanceof Element) {
                childNodesCollection.add((Element) childNodes.item(i));
            }
        }

        return mapChildElements(childNodesCollection);
    }

    public static LinkedListMultimap<QName, Element> mapChildElements(Collection<Element> childNodesCollection) {
        LinkedListMultimap<QName, Element> mappedChildElements = LinkedListMultimap.create();

        for (Element element : childNodesCollection) {
            QName childQName = XmlDocumentUtils.qNameFromElement(element);
            mappedChildElements.put(childQName, element);
        }

        return mappedChildElements;
    }

    public static Map<QName, ChoiceNode> mapChildElementsFromChoices(DataNodeContainer schema) {
        Map<QName, ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (final DataSchemaNode childSchema : schema.getChildNodes()) {
            if(childSchema instanceof ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) childSchema).getCases()) {

                    for (QName qName : getChildNodes(choiceCaseNode)) {
                        mappedChoices.put(qName, (ChoiceNode) childSchema);
                    }
                }
            }
        }

        return mappedChoices;
    }

    public static Map<QName, AugmentationSchema> mapChildElementsFromAugments(AugmentationTarget schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newLinkedHashMap();

        for (final AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (QName qName : getChildNodes(augmentationSchema)) {
                mappedAugments.put(qName, augmentationSchema);
            }
        }

        return mappedAugments;
    }

    public static Set<QName> getChildNodes(DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = Sets.newHashSet();

        for (DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if(childSchema instanceof ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) childSchema).getCases()) {
                    allChildNodes.addAll(getChildNodes(choiceCaseNode));
                }
            }
            else {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }

    public static Map<QName, String> toAttributes(NamedNodeMap xmlAttributes){
        Map<QName, String> attributes = new HashMap<>();

        for(int i=0; i < xmlAttributes.getLength();i++){
            Node node = xmlAttributes.item(i);
            try {
                String namespace = node.getNamespaceURI();
                if(namespace == null){
                    namespace = "";
                }
                QName qName = new QName(new URI(namespace), node.getLocalName());
                attributes.put(qName, node.getNodeValue());
            } catch (URISyntaxException e) {
                Exceptions.sneakyThrow(e);
            }
        }
        return attributes;
    }


}
