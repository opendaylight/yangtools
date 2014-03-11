/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.json;

import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadataNode;
import java.util.*;

public class JsonCnSnUtils {
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
        return findSchemaForChild(schema, qname, childNodes);
    }

    public static DataSchemaNode findSchemaForChild(DataNodeContainer schema, QName qname, Set<DataSchemaNode> childNodes) {
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

    public static LinkedListMultimap<QName, Node<?>> mapChildElementsForSingletonNode(Node<?> node) {
        return mapChildElements( ((CompositeNode)node).getValue());
    }

    public static LinkedListMultimap<QName, Node<?>> mapChildElements(Collection<Node<?>> childNodesCollection) {
        LinkedListMultimap<QName, Node<?>> mappedChildElements = LinkedListMultimap.create();

        for (Node<?> node : childNodesCollection) {
            mappedChildElements.put(node.getNodeType(), node);
        }

        return mappedChildElements;
    }


    public static Map<QName,ChoiceNode> mapChildElementsFromChoicesInAugment(AugmentationSchema schema, Set<DataSchemaNode> realChildSchemas) {
        Map<QName, ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (DataSchemaNode realChildSchema : realChildSchemas) {
            if(realChildSchema instanceof ChoiceNode)
                mappedChoices.putAll(mapChildElementsFromChoices(schema, realChildSchemas));
        }

        return mappedChoices;
    }

    public static Map<QName, ChoiceNode> mapChildElementsFromChoices(DataNodeContainer schema) {
        Set<DataSchemaNode> childNodes = schema.getChildNodes();

        return mapChildElementsFromChoices(schema, childNodes);
    }

    private static Map<QName, ChoiceNode> mapChildElementsFromChoices(DataNodeContainer schema, Set<DataSchemaNode> childNodes) {
        Map<QName, ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (final DataSchemaNode childSchema : childNodes) {
            if(childSchema instanceof ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) childSchema).getCases()) {

                    for (QName qName : getChildNodes(choiceCaseNode)) {
                        mappedChoices.put(qName, (ChoiceNode) childSchema);
                    }
                }
            }
        }

        if(schema instanceof AugmentationTarget == false) {
            return mappedChoices;
        }

        // Remove augmented choices
        final Map<QName, AugmentationSchema> augments = mapChildElementsFromAugments((AugmentationTarget) schema);

        return Maps.filterKeys(mappedChoices, new Predicate<QName>() {
            @Override
            public boolean apply(QName input) {
                return augments.containsKey(input) == false;
            }
        });
    }

    public static Map<QName, AugmentationSchema> mapChildElementsFromAugments(AugmentationTarget schema) {
        Map<QName, AugmentationSchema> mappedAugments = Maps.newLinkedHashMap();


        Map<QName, AugmentationSchema> augments = Maps.newHashMap();
        for (final AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                augments.put(dataSchemaNode.getQName(), augmentationSchema);
            }
        }

        if(schema instanceof DataNodeContainer) {

        for (DataSchemaNode child : ((DataNodeContainer)schema).getChildNodes()) {
            if(augments.containsKey(child.getQName())) {

                if(child instanceof DataNodeContainer) {
                    for (QName qName : getChildNodes((DataNodeContainer) child)) {
                        mappedAugments.put(qName,  augments.get(child.getQName()));
                    }
                } else if(child instanceof ChoiceNode){
                    for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) child).getCases()) {
                        for (QName qName : getChildNodes(choiceCaseNode)) {
                            mappedAugments.put(qName,  augments.get(child.getQName()));
                        }
                    }
                } else {
                    mappedAugments.put(child.getQName(), augments.get(child.getQName()));
                }
            }
        }

        }

        if(schema instanceof ChoiceNode) {
            for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) schema).getCases()) {
                if(augments.containsKey(choiceCaseNode.getQName()) == false) {
                    continue;
                }

                for (QName qName : getChildNodes(choiceCaseNode)) {
                    mappedAugments.put(qName, augments.get(choiceCaseNode.getQName()));
                }
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
            else if (childSchema instanceof DataNodeContainer) {
                allChildNodes.addAll(getChildNodes((DataNodeContainer) childSchema));
            }
            else {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }

    public static Set<DataSchemaNode> getRealSchemasForAugment(AugmentationTarget schema, AugmentationSchema augmentSchema) {
        if(schema.getAvailableAugmentations().contains(augmentSchema) == false) {
            return Collections.emptySet();
        }

        Set<DataSchemaNode> realChildNodes = Sets.newHashSet();

        if(schema instanceof DataNodeContainer) {
            for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
                DataSchemaNode realChild = ((DataNodeContainer)schema).getDataChildByName(dataSchemaNode.getQName());
                realChildNodes.add(realChild);
            }
        } else if(schema instanceof ChoiceNode) {
            for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) schema).getCases()) {
                    if(getChildNodes(choiceCaseNode).contains(dataSchemaNode.getQName())) {
                        realChildNodes.add(choiceCaseNode.getDataChildByName(dataSchemaNode.getQName()));
                    }
                }
            }
        }

        return realChildNodes;
    }


}
