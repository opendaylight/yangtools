/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class SchemaUtils {
    private SchemaUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param qname - schema node to find
     * @param dataSchemaNode - iterable of schemaNodes to look through
     * @return - schema node with newest revision or absent if no schema node with matching qname is found
     */
    public static Optional<DataSchemaNode> findFirstSchema(final QName qname, final Iterable<DataSchemaNode> dataSchemaNode) {
        DataSchemaNode sNode = null;
        if (dataSchemaNode != null && qname != null) {
            for (DataSchemaNode dsn : dataSchemaNode) {
                if (qname.isEqualWithoutRevision(dsn.getQName())) {
                    if (sNode == null || sNode.getQName().getRevision().compareTo(dsn.getQName().getRevision()) < 0) {
                        sNode = dsn;
                    }
                } else if (dsn instanceof ChoiceSchemaNode) {
                    for (ChoiceCaseNode choiceCase : ((ChoiceSchemaNode) dsn).getCases()) {

                        final DataSchemaNode dataChildByName = choiceCase.getDataChildByName(qname);
                        if (dataChildByName != null) {
                            return Optional.of(dataChildByName);
                        }
                        Optional<DataSchemaNode> foundDsn = findFirstSchema(qname, choiceCase.getChildNodes());
                        if (foundDsn.isPresent()) {
                            return foundDsn;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(sNode);
    }

    /**
     *
     * Find child schema node identified by its QName within a provided schema node
     *
     * @param schema schema for parent node - search root
     * @param qname qname(with or without a revision) of a child node to be found in the parent schema
     * @return found schema node
     * @throws java.lang.IllegalStateException if the child was not found in parent schema node
     */
    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname) {
        // Try to find child schema node directly, but use a fallback that compares QNames without revisions and auto-expands choices
        final DataSchemaNode dataChildByName = schema.getDataChildByName(qname);
        return dataChildByName == null ? findSchemaForChild(schema, qname, schema.getChildNodes()) : dataChildByName;
    }

    @Nullable
    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname, final boolean strictMode) {
        if (strictMode) {
            return findSchemaForChild(schema, qname);
        }

        Optional<DataSchemaNode> childSchemaOptional = findFirstSchema(qname, schema.getChildNodes());
        if (!childSchemaOptional.isPresent()) {
            return null;
        }
        return childSchemaOptional.get();
    }

    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname, final Iterable<DataSchemaNode> childNodes) {
        Optional<DataSchemaNode> childSchema = findFirstSchema(qname, childNodes);
        Preconditions.checkState(childSchema.isPresent(),
                "Unknown child(ren) node(s) detected, identified by: %s, in: %s", qname, schema);
        return childSchema.get();
    }

    public static AugmentationSchema findSchemaForAugment(final AugmentationTarget schema, final Set<QName> qNames) {
        Optional<AugmentationSchema> schemaForAugment = findAugment(schema, qNames);
        Preconditions.checkState(schemaForAugment.isPresent(), "Unknown augmentation node detected, identified by: %s, in: %s",
                qNames, schema);
        return schemaForAugment.get();
    }

    public static AugmentationSchema findSchemaForAugment(final ChoiceSchemaNode schema, final Set<QName> qNames) {
        Optional<AugmentationSchema> schemaForAugment = Optional.empty();

        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            schemaForAugment = findAugment(choiceCaseNode, qNames);
            if (schemaForAugment.isPresent()) {
                break;
            }
        }

        Preconditions.checkState(schemaForAugment.isPresent(), "Unknown augmentation node detected, identified by: %s, in: %s",
                qNames, schema);
        return schemaForAugment.get();
    }

    private static Optional<AugmentationSchema> findAugment(final AugmentationTarget schema, final Set<QName> qNames) {
        for (AugmentationSchema augment : schema.getAvailableAugmentations()) {
            HashSet<QName> qNamesFromAugment = Sets.newHashSet(Collections2.transform(augment.getChildNodes(),
                DataSchemaNode::getQName));

            if (qNamesFromAugment.equals(qNames)) {
                return Optional.of(augment);
            }
        }

        return Optional.empty();
    }

    public static DataSchemaNode findSchemaForChild(final ChoiceSchemaNode schema, final QName childPartialQName) {
        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            Optional<DataSchemaNode> childSchema = findFirstSchema(childPartialQName, choiceCaseNode.getChildNodes());
            if (childSchema.isPresent()) {
                return childSchema.get();
            }
        }


        throw new IllegalStateException(String.format("Unknown child(ren) node(s) detected, identified by: %s, in: %s",
                childPartialQName, schema));
    }

    /**
     * Recursively find all child nodes that come from choices.
     *
     * @param schema schema
     * @return Map with all child nodes, to their most top augmentation
     */
    public static Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final DataNodeContainer schema) {
        return mapChildElementsFromChoices(schema, schema.getChildNodes());
    }

    private static Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final DataNodeContainer schema, final Iterable<DataSchemaNode> childNodes) {
        Map<QName, ChoiceSchemaNode> mappedChoices = Maps.newLinkedHashMap();

        for (final DataSchemaNode childSchema : childNodes) {
            if (childSchema instanceof ChoiceSchemaNode) {

                if (isFromAugment(schema, childSchema)) {
                    continue;
                }

                for (ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) childSchema).getCases()) {

                    for (QName qName : getChildNodesRecursive(choiceCaseNode)) {
                        mappedChoices.put(qName, (ChoiceSchemaNode) childSchema);
                    }
                }
            }
        }

        return mappedChoices;
    }

    private static boolean isFromAugment(final DataNodeContainer schema, final DataSchemaNode childSchema) {
        if (!(schema instanceof AugmentationTarget)) {
            return false;
        }

        for (AugmentationSchema augmentationSchema : ((AugmentationTarget) schema).getAvailableAugmentations()) {
            if (augmentationSchema.getDataChildByName(childSchema.getQName()) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Recursively find all child nodes that come from augmentations.
     *
     * @param schema schema
     * @return Map with all child nodes, to their most top augmentation
     */
    public static Map<QName, AugmentationSchema> mapChildElementsFromAugments(final AugmentationTarget schema) {

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
                if (!augments.containsKey(child.getQName())) {
                    continue;
                }

                AugmentationSchema mostTopAugmentation = augments.get(child.getQName());

                // recursively add all child nodes in case of augment, case and choice
                if (child instanceof AugmentationSchema || child instanceof ChoiceCaseNode) {
                    for (QName qName : getChildNodesRecursive((DataNodeContainer) child)) {
                        childNodesToAugmentation.put(qName, mostTopAugmentation);
                    }
                } else if (child instanceof ChoiceSchemaNode) {
                    for (ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) child).getCases()) {
                        for (QName qName : getChildNodesRecursive(choiceCaseNode)) {
                            childNodesToAugmentation.put(qName, mostTopAugmentation);
                        }
                    }
                } else {
                    childNodesToAugmentation.put(child.getQName(), mostTopAugmentation);
                }
            }
        }

        // Choice Node has to map child nodes from all its cases
        if (schema instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) schema).getCases()) {
                if (!augments.containsKey(choiceCaseNode.getQName())) {
                    continue;
                }

                for (QName qName : getChildNodesRecursive(choiceCaseNode)) {
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
     *
     * @param nodeContainer node container
     * @return set of QNames
     */
    public static Set<QName> getChildNodesRecursive(final DataNodeContainer nodeContainer) {
        Set<QName> allChildNodes = Sets.newHashSet();

        for (DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if (childSchema instanceof ChoiceSchemaNode) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) childSchema).getCases()) {
                    allChildNodes.addAll(getChildNodesRecursive(choiceCaseNode));
                }
            } else if (childSchema instanceof AugmentationSchema || childSchema instanceof ChoiceCaseNode) {
                allChildNodes.addAll(getChildNodesRecursive((DataNodeContainer) childSchema));
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
     * Schema of child node from augment is incomplete, therefore its useless for XML/NormalizedNode translation.
     *
     * @param targetSchema target schema
     * @param augmentSchema augment schema
     * @return set of nodes
     */
    public static Set<DataSchemaNode> getRealSchemasForAugment(final AugmentationTarget targetSchema, final AugmentationSchema augmentSchema) {
        if (!targetSchema.getAvailableAugmentations().contains(augmentSchema)) {
            return Collections.emptySet();
        }

        Set<DataSchemaNode> realChildNodes = Sets.newHashSet();

        if (targetSchema instanceof DataNodeContainer) {
            realChildNodes = getRealSchemasForAugment((DataNodeContainer)targetSchema, augmentSchema);
        } else if (targetSchema instanceof ChoiceSchemaNode) {
            for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
                for (ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) targetSchema).getCases()) {
                    if (getChildNodesRecursive(choiceCaseNode).contains(dataSchemaNode.getQName())) {
                        realChildNodes.add(choiceCaseNode.getDataChildByName(dataSchemaNode.getQName()));
                    }
                }
            }
        }

        return realChildNodes;
    }

    public static Set<DataSchemaNode> getRealSchemasForAugment(final DataNodeContainer targetSchema,
            final AugmentationSchema augmentSchema) {
        Set<DataSchemaNode> realChildNodes = Sets.newHashSet();
        for (DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
            DataSchemaNode realChild = targetSchema.getDataChildByName(dataSchemaNode.getQName());
            realChildNodes.add(realChild);
        }
        return realChildNodes;
    }

    public static Optional<ChoiceCaseNode> detectCase(final ChoiceSchemaNode schema, final DataContainerChild<?, ?> child) {
        for (ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            if (child instanceof AugmentationNode
                    && belongsToCaseAugment(choiceCaseNode, (AugmentationIdentifier) child.getIdentifier())) {
                return Optional.of(choiceCaseNode);
            } else if (choiceCaseNode.getDataChildByName(child.getNodeType()) != null) {
                return Optional.of(choiceCaseNode);
            }
        }

        return Optional.empty();
    }

    public static boolean belongsToCaseAugment(final ChoiceCaseNode caseNode, final AugmentationIdentifier childToProcess) {
        for (AugmentationSchema augmentationSchema : caseNode.getAvailableAugmentations()) {

            Set<QName> currentAugmentChildNodes = Sets.newHashSet();
            for (DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                currentAugmentChildNodes.add(dataSchemaNode.getQName());
            }

            if (childToProcess.getPossibleChildNames().equals(currentAugmentChildNodes)){
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to find in {@code parent} which is dealed as augmentation target node with QName as {@code child}. If such
     * node is found then it is returned, else null.
     *
     * @param parent parent node
     * @param child child node
     * @return augmentation schema
     */
    public static AugmentationSchema findCorrespondingAugment(final DataSchemaNode parent, final DataSchemaNode child) {
        if (parent instanceof AugmentationTarget && !(parent instanceof ChoiceSchemaNode)) {
            for (AugmentationSchema augmentation : ((AugmentationTarget) parent).getAvailableAugmentations()) {
                DataSchemaNode childInAugmentation = augmentation.getDataChildByName(child.getQName());
                if (childInAugmentation != null) {
                    return augmentation;
                }
            }
        }
        return null;
    }

    public static AugmentationIdentifier getNodeIdentifierForAugmentation(final AugmentationSchema schema) {
        final Collection<QName> qnames = Collections2.transform(schema.getChildNodes(), DataSchemaNode::getQName);
        return new AugmentationIdentifier(ImmutableSet.copyOf(qnames));
    }

    /**
     * Finds schema node for given path in schema context.
     * @param schemaContext schema context
     * @param path path
     * @return schema node on path
     */
    public static SchemaNode findParentSchemaOnPath(final SchemaContext schemaContext, final SchemaPath path) {
        SchemaNode current = Preconditions.checkNotNull(schemaContext);
        for (final QName qname : path.getPathFromRoot()) {
            current = findChildSchemaByQName(current, qname);
        }
        return current;
    }

    /**
     * Find child schema node identified by its QName within a provided schema node.
     * @param node schema node
     * @param qname QName
     * @return child schema node
     * @throws java.lang.IllegalArgumentException if the schema node does not allow children
     */
    public static SchemaNode findChildSchemaByQName(final SchemaNode node, final QName qname) {
        SchemaNode child = null;

        if (node instanceof DataNodeContainer) {
            child = ((DataNodeContainer) node).getDataChildByName(qname);

            if (child == null && node instanceof SchemaContext) {
                child = tryFindGroupings((SchemaContext) node, qname).orElse(null);
            }

            if (child == null && node instanceof SchemaContext) {
                child = tryFindNotification((SchemaContext) node, qname)
                        .orElse(tryFindRpc((SchemaContext) node, qname).orElse(null));
            }
        } else if (node instanceof ChoiceSchemaNode) {
            child = ((ChoiceSchemaNode) node).getCaseNodeByName(qname);
        } else if (node instanceof RpcDefinition) {
            switch (qname.getLocalName()) {
                case "input":
                    child = ((RpcDefinition) node).getInput();
                    break;
                case "output":
                    child = ((RpcDefinition) node).getOutput();
                    break;
                default:
                    child = null;
                    break;
            }
        } else {
            throw new IllegalArgumentException(String.format("Schema node %s does not allow children.", node));
        }

        return child;
    }

    private static Optional<SchemaNode> tryFindGroupings(final SchemaContext ctx, final QName qname) {
        return Optional.ofNullable(Iterables.find(ctx.getGroupings(), new SchemaNodePredicate(qname), null));
    }

    private static Optional<SchemaNode> tryFindRpc(final SchemaContext ctx, final QName qname) {
        return Optional.ofNullable(Iterables.find(ctx.getOperations(), new SchemaNodePredicate(qname), null));
    }

    private static Optional<SchemaNode> tryFindNotification(final SchemaContext ctx, final QName qname) {
        return Optional.ofNullable(Iterables.find(ctx.getNotifications(), new SchemaNodePredicate(qname), null));
    }

    private static final class SchemaNodePredicate implements Predicate<SchemaNode> {
        private final QName qname;

        public SchemaNodePredicate(final QName qname) {
            this.qname = qname;
        }

        @Override
        public boolean apply(final SchemaNode input) {
            return input.getQName().equals(qname);
        }
    }

}
