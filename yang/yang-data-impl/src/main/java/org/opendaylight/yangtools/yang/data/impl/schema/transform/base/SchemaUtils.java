/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SchemaUtils {
    
    private SchemaUtils() {
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

    /**
     * Recursively find all child nodes that come from choices in augment.
     *
     * @return Map with all child nodes, to their most top augmentation
     */
    public static Map<QName,ChoiceNode> mapChildElementsFromChoicesInAugment(AugmentationSchema schema, Set<DataSchemaNode> realChildSchemas) {
        Map<QName, ChoiceNode> mappedChoices = Maps.newLinkedHashMap();

        for (DataSchemaNode realChildSchema : realChildSchemas) {
            if(realChildSchema instanceof ChoiceNode)
                mappedChoices.putAll(mapChildElementsFromChoices(schema, realChildSchemas));
        }

        return mappedChoices;
    }

    /**
     * Recursively find all child nodes that come from choices.
     *
     * @return Map with all child nodes, to their most top augmentation
     */
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

        // Remove augmented choices
        // TODO ineffective, mapping augments one more time is not necessary, the map could be injected
        if(schema instanceof AugmentationTarget) {
            final Map<QName, AugmentationSchema> augments = mapChildElementsFromAugments((AugmentationTarget) schema);

            return Maps.filterKeys(mappedChoices, new Predicate<QName>() {
                @Override
                public boolean apply(QName input) {
                    return augments.containsKey(input) == false;
                }
            });
        }

        return mappedChoices;
    }

    /**
     * Recursively find all child nodes that come from augmentations.
     *
     * @return Map with all child nodes, to their most top augmentation
     */
    public static Map<QName, AugmentationSchema> mapChildElementsFromAugments(AugmentationTarget schema) {

        Map<QName, AugmentationSchema> childNodesToAugmentation = Maps.newLinkedHashMap();

        // Find QNames of augmented child nodes
        Map<QName, AugmentationSchema> augments = Maps.newHashMap();
        for (final AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                augments.put(dataSchemaNode.getQName(), augmentationSchema);
            }
        }

        // Augmented nodes have to be looked up directly in augmentationTarget
        // because nodes from augment do not contain nodes from other augmentations
        if (schema instanceof DataNodeContainer) {

            for (DataSchemaNode child : ((DataNodeContainer) schema).getChildNodes()) {
                // If is not augmented child, continue
                if (augments.containsKey(child.getQName()) == false)
                    continue;

                AugmentationSchema mostTopAugmentation = augments.get(child.getQName());

                // recursively add all child nodes in case of augment, case and choice
                if (child instanceof AugmentationSchema || child instanceof ChoiceCaseNode) {
                    for (QName qName : getChildNodes((DataNodeContainer) child)) {
                        childNodesToAugmentation.put(qName, mostTopAugmentation);
                    }
                } else if (child instanceof ChoiceNode) {
                    for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) child).getCases()) {
                        for (QName qName : getChildNodes(choiceCaseNode)) {
                            childNodesToAugmentation.put(qName, mostTopAugmentation);
                        }
                    }
                } else {
                    childNodesToAugmentation.put(child.getQName(), mostTopAugmentation);
                }
            }
        }

        // Choice Node has to map child nodes from all its cases
        if (schema instanceof ChoiceNode) {
            for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) schema).getCases()) {
                if (augments.containsKey(choiceCaseNode.getQName()) == false) {
                    continue;
                }

                for (QName qName : getChildNodes(choiceCaseNode)) {
                    childNodesToAugmentation.put(qName, augments.get(choiceCaseNode.getQName()));
                }
            }
        }

        return childNodesToAugmentation;
    }

    /**
     * Recursively list all child nodes.
     *
     * In case of choice, augment and cases, step in.
     */
    public static Set<QName> getChildNodes(DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = Sets.newHashSet();

        for (DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if(childSchema instanceof ChoiceNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) childSchema).getCases()) {
                    allChildNodes.addAll(getChildNodes(choiceCaseNode));
                }
            } else if(childSchema instanceof AugmentationSchema || childSchema instanceof ChoiceCaseNode) {
                allChildNodes.addAll(getChildNodes((DataNodeContainer) childSchema));
            }
            else {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }

    /**
     * Retrieves real schemas for augmented child node.
     *
     * Schema of the same child node from augment, and directly from target is not the same.
     * Schema of child node from augment is incomplete, therefore its useless for xml <-> normalizedNode translation.
     *
     */
    public static Set<DataSchemaNode> getRealSchemasForAugment(AugmentationTarget targetSchema, AugmentationSchema augmentSchema) {
        if(targetSchema.getAvailableAugmentations().contains(augmentSchema) == false) {
            return Collections.emptySet();
        }

        Set<DataSchemaNode> realChildNodes = Sets.newHashSet();

        if(targetSchema instanceof DataNodeContainer) {
              realChildNodes = getRealSchemasForAugment((DataNodeContainer)targetSchema, augmentSchema);
        } else if(targetSchema instanceof ChoiceNode) {
            for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceNode) targetSchema).getCases()) {
                    if(getChildNodes(choiceCaseNode).contains(dataSchemaNode.getQName())) {
                        realChildNodes.add(choiceCaseNode.getDataChildByName(dataSchemaNode.getQName()));
                    }
                }
            }
        }

        return realChildNodes;
    }

    public static Set<DataSchemaNode> getRealSchemasForAugment(DataNodeContainer targetSchema,
            AugmentationSchema augmentSchema) {
        Set<DataSchemaNode> realChildNodes = Sets.newHashSet();
        for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
            DataSchemaNode realChild = ((DataNodeContainer) targetSchema).getDataChildByName(dataSchemaNode.getQName());
            realChildNodes.add(realChild);
        }

        return realChildNodes;
    }

    public static Optional<ChoiceCaseNode> detectCase(ChoiceNode schema, DataContainerChild<?, ?> child) {
        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            for (DataSchemaNode childFromCase : choiceCaseNode.getChildNodes()) {
                if (childFromCase.getQName().equals(child.getNodeType())) {
                    return Optional.of(choiceCaseNode);
                }
            }
        }

        return Optional.absent();
    }
}
