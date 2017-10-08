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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class SchemaUtils {
    private SchemaUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Find the first schema with specified QName.
     *
     * @param qname schema node to find
     * @param dataSchemaNode Iterable of schemaNodes to look through
     * @return schema node with newest revision or absent if no schema node with matching qname is found
     */
    public static Optional<DataSchemaNode> findFirstSchema(final QName qname,
            final Iterable<DataSchemaNode> dataSchemaNode) {
        DataSchemaNode schema = null;
        if (dataSchemaNode != null && qname != null) {
            for (final DataSchemaNode dsn : dataSchemaNode) {
                if (qname.isEqualWithoutRevision(dsn.getQName())) {
                    if (schema == null || schema.getQName().getRevision().compareTo(dsn.getQName().getRevision()) < 0) {
                        schema = dsn;
                    }
                } else if (dsn instanceof ChoiceSchemaNode) {
                    for (final ChoiceCaseNode choiceCase : ((ChoiceSchemaNode) dsn).getCases()) {

                        final DataSchemaNode dataChildByName = choiceCase.getDataChildByName(qname);
                        if (dataChildByName != null) {
                            return Optional.of(dataChildByName);
                        }
                        final Optional<DataSchemaNode> foundDsn = findFirstSchema(qname, choiceCase.getChildNodes());
                        if (foundDsn.isPresent()) {
                            return foundDsn;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(schema);
    }

    /**
     * Find child schema node identified by its QName within a provided schema node.
     *
     * @param schema schema for parent node - search root
     * @param qname qname(with or without a revision) of a child node to be found in the parent schema
     * @return found schema node
     * @throws java.lang.IllegalStateException if the child was not found in parent schema node
     */
    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname) {
        // Try to find child schema node directly, but use a fallback that compares QNames without revisions
        // and auto-expands choices
        final DataSchemaNode dataChildByName = schema.getDataChildByName(qname);
        return dataChildByName == null ? findSchemaForChild(schema, qname, schema.getChildNodes()) : dataChildByName;
    }

    @Nullable
    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname,
            final boolean strictMode) {
        if (strictMode) {
            return findSchemaForChild(schema, qname);
        }

        final Optional<DataSchemaNode> childSchemaOptional = findFirstSchema(qname, schema.getChildNodes());
        if (!childSchemaOptional.isPresent()) {
            return null;
        }
        return childSchemaOptional.get();
    }

    public static DataSchemaNode findSchemaForChild(final DataNodeContainer schema, final QName qname,
            final Iterable<DataSchemaNode> childNodes) {
        final Optional<DataSchemaNode> childSchema = findFirstSchema(qname, childNodes);
        Preconditions.checkState(childSchema.isPresent(),
                "Unknown child(ren) node(s) detected, identified by: %s, in: %s", qname, schema);
        return childSchema.get();
    }

    public static DataSchemaNode findSchemaForChild(final ChoiceSchemaNode schema, final QName childPartialQName) {
        for (final ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            final Optional<DataSchemaNode> childSchema = findFirstSchema(childPartialQName,
                choiceCaseNode.getChildNodes());
            if (childSchema.isPresent()) {
                return childSchema.get();
            }
        }


        throw new IllegalStateException(String.format("Unknown child(ren) node(s) detected, identified by: %s, in: %s",
                childPartialQName, schema));
    }

    public static AugmentationSchema findSchemaForAugment(final AugmentationTarget schema, final Set<QName> qnames) {
        final Optional<AugmentationSchema> schemaForAugment = findAugment(schema, qnames);
        Preconditions.checkState(schemaForAugment.isPresent(),
            "Unknown augmentation node detected, identified by: %s, in: %s", qnames, schema);
        return schemaForAugment.get();
    }

    public static AugmentationSchema findSchemaForAugment(final ChoiceSchemaNode schema, final Set<QName> qnames) {
        Optional<AugmentationSchema> schemaForAugment = Optional.empty();

        for (final ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            schemaForAugment = findAugment(choiceCaseNode, qnames);
            if (schemaForAugment.isPresent()) {
                break;
            }
        }

        Preconditions.checkState(schemaForAugment.isPresent(),
            "Unknown augmentation node detected, identified by: %s, in: %s", qnames, schema);
        return schemaForAugment.get();
    }

    private static Optional<AugmentationSchema> findAugment(final AugmentationTarget schema, final Set<QName> qnames) {
        for (final AugmentationSchema augment : schema.getAvailableAugmentations()) {
            final Set<QName> qNamesFromAugment = ImmutableSet.copyOf(Collections2.transform(augment.getChildNodes(),
                DataSchemaNode::getQName));

            if (qNamesFromAugment.equals(qnames)) {
                return Optional.of(augment);
            }
        }

        return Optional.empty();
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

    private static Map<QName, ChoiceSchemaNode> mapChildElementsFromChoices(final DataNodeContainer schema,
            final Iterable<DataSchemaNode> childNodes) {
        final Map<QName, ChoiceSchemaNode> mappedChoices = new LinkedHashMap<>();

        for (final DataSchemaNode childSchema : childNodes) {
            if (childSchema instanceof ChoiceSchemaNode) {

                if (isFromAugment(schema, childSchema)) {
                    continue;
                }

                for (final ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) childSchema).getCases()) {
                    for (final QName qname : getChildNodesRecursive(choiceCaseNode)) {
                        mappedChoices.put(qname, (ChoiceSchemaNode) childSchema);
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

        for (final AugmentationSchema augmentationSchema : ((AugmentationTarget) schema).getAvailableAugmentations()) {
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

        final Map<QName, AugmentationSchema> childNodesToAugmentation = new LinkedHashMap<>();

        // Find QNames of augmented child nodes
        final Map<QName, AugmentationSchema> augments = new HashMap<>();
        for (final AugmentationSchema augmentationSchema : schema.getAvailableAugmentations()) {
            for (final DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                augments.put(dataSchemaNode.getQName(), augmentationSchema);
            }
        }

        // Augmented nodes have to be looked up directly in augmentationTarget
        // because nodes from augment do not contain nodes from other augmentations
        if (schema instanceof DataNodeContainer) {

            for (final DataSchemaNode child : ((DataNodeContainer) schema).getChildNodes()) {
                // If is not augmented child, continue
                if (!augments.containsKey(child.getQName())) {
                    continue;
                }

                final AugmentationSchema mostTopAugmentation = augments.get(child.getQName());

                // recursively add all child nodes in case of augment, case and choice
                if (child instanceof AugmentationSchema || child instanceof ChoiceCaseNode) {
                    for (final QName qname : getChildNodesRecursive((DataNodeContainer) child)) {
                        childNodesToAugmentation.put(qname, mostTopAugmentation);
                    }
                } else if (child instanceof ChoiceSchemaNode) {
                    for (final ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) child).getCases()) {
                        for (final QName qname : getChildNodesRecursive(choiceCaseNode)) {
                            childNodesToAugmentation.put(qname, mostTopAugmentation);
                        }
                    }
                } else {
                    childNodesToAugmentation.put(child.getQName(), mostTopAugmentation);
                }
            }
        }

        // Choice Node has to map child nodes from all its cases
        if (schema instanceof ChoiceSchemaNode) {
            for (final ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) schema).getCases()) {
                if (!augments.containsKey(choiceCaseNode.getQName())) {
                    continue;
                }

                for (final QName qname : getChildNodesRecursive(choiceCaseNode)) {
                    childNodesToAugmentation.put(qname, augments.get(choiceCaseNode.getQName()));
                }
            }
        }

        return childNodesToAugmentation;
    }

    /**
     * Recursively list all child nodes. In case of choice, augment and cases, step in.
     *
     * @param nodeContainer node container
     * @return set of QNames
     */
    public static Set<QName> getChildNodesRecursive(final DataNodeContainer nodeContainer) {
        final Set<QName> allChildNodes = new HashSet<>();

        for (final DataSchemaNode childSchema : nodeContainer.getChildNodes()) {
            if (childSchema instanceof ChoiceSchemaNode) {
                for (final ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) childSchema).getCases()) {
                    allChildNodes.addAll(getChildNodesRecursive(choiceCaseNode));
                }
            } else if (childSchema instanceof AugmentationSchema || childSchema instanceof ChoiceCaseNode) {
                allChildNodes.addAll(getChildNodesRecursive((DataNodeContainer) childSchema));
            } else {
                allChildNodes.add(childSchema.getQName());
            }
        }

        return allChildNodes;
    }

    /**
     * Retrieves real schemas for augmented child node.
     *
     * <p>
     * Schema of the same child node from augment, and directly from target is not the same.
     * Schema of child node from augment is incomplete, therefore its useless for XML/NormalizedNode translation.
     *
     * @param targetSchema target schema
     * @param augmentSchema augment schema
     * @return set of nodes
     */
    public static Set<DataSchemaNode> getRealSchemasForAugment(final AugmentationTarget targetSchema,
            final AugmentationSchema augmentSchema) {
        if (!targetSchema.getAvailableAugmentations().contains(augmentSchema)) {
            return Collections.emptySet();
        }
        if (targetSchema instanceof DataNodeContainer) {
            return getRealSchemasForAugment((DataNodeContainer)targetSchema, augmentSchema);
        }
        final Set<DataSchemaNode> realChildNodes = new HashSet<>();
        if (targetSchema instanceof ChoiceSchemaNode) {
            for (final DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
                for (final ChoiceCaseNode choiceCaseNode : ((ChoiceSchemaNode) targetSchema).getCases()) {
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
        final Set<DataSchemaNode> realChildNodes = new HashSet<>();
        for (final DataSchemaNode dataSchemaNode : augmentSchema.getChildNodes()) {
            final DataSchemaNode realChild = targetSchema.getDataChildByName(dataSchemaNode.getQName());
            realChildNodes.add(realChild);
        }
        return realChildNodes;
    }

    public static Optional<ChoiceCaseNode> detectCase(final ChoiceSchemaNode schema,
            final DataContainerChild<?, ?> child) {
        for (final ChoiceCaseNode choiceCaseNode : schema.getCases()) {
            if (child instanceof AugmentationNode
                    && belongsToCaseAugment(choiceCaseNode, (AugmentationIdentifier) child.getIdentifier())) {
                return Optional.of(choiceCaseNode);
            } else if (choiceCaseNode.getDataChildByName(child.getNodeType()) != null) {
                return Optional.of(choiceCaseNode);
            }
        }

        return Optional.empty();
    }

    public static boolean belongsToCaseAugment(final ChoiceCaseNode caseNode,
            final AugmentationIdentifier childToProcess) {
        for (final AugmentationSchema augmentationSchema : caseNode.getAvailableAugmentations()) {

            final Set<QName> currentAugmentChildNodes = new HashSet<>();
            for (final DataSchemaNode dataSchemaNode : augmentationSchema.getChildNodes()) {
                currentAugmentChildNodes.add(dataSchemaNode.getQName());
            }

            if (childToProcess.getPossibleChildNames().equals(currentAugmentChildNodes)) {
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
            for (final AugmentationSchema augmentation : ((AugmentationTarget) parent).getAvailableAugmentations()) {
                final DataSchemaNode childInAugmentation = augmentation.getDataChildByName(child.getQName());
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
     * Finds schema node for given path in schema context. This method performs
     * lookup in the namespace of all leafs, leaf-lists, lists, containers,
     * choices, rpcs, actions, notifications, anydatas, and anyxmls according to
     * Rfc6050/Rfc7950 section 6.2.1.
     *
     * @param schemaContext
     *            schema context
     * @param path
     *            path
     * @return schema node on path
     */
    public static SchemaNode findDataParentSchemaOnPath(final SchemaContext schemaContext, final SchemaPath path) {
        SchemaNode current = Preconditions.checkNotNull(schemaContext);
        for (final QName qname : path.getPathFromRoot()) {
            current = findDataChildSchemaByQName(current, qname);
        }
        return current;
    }

    /**
     * Finds schema node for given path in schema context. This method performs lookup in both the namespace
     * of groupings and the namespace of all leafs, leaf-lists, lists, containers, choices, rpcs, actions,
     * notifications, anydatas and anyxmls according to Rfc6050/Rfc7950 section 6.2.1.
     *
     * <p>
     * This method is deprecated, because name conflicts can occur between the namespace of groupings and namespace
     * of data nodes and in consequence lookup could be ambiguous.
     *
     * @param schemaContext
     *            schema context
     * @param path
     *            path
     * @return schema node on path
     *
     * @deprecated Use {@link #findParentSchemaNodesOnPath(SchemaContext, SchemaPath)} instead.
     */
    @Deprecated
    public static SchemaNode findParentSchemaOnPath(final SchemaContext schemaContext, final SchemaPath path) {
        SchemaNode current = Preconditions.checkNotNull(schemaContext);
        for (final QName qname : path.getPathFromRoot()) {
            current = findChildSchemaByQName(current, qname);
        }
        return current;
    }

    /**
     * Find child data schema node identified by its QName within a provided schema node. This method performs lookup
     * in the namespace of all leafs, leaf-lists, lists, containers, choices, rpcs, actions, notifications, anydatas
     * and anyxmls according to Rfc6050/Rfc7950 section 6.2.1.
     *
     * @param node
     *            schema node
     * @param qname
     *            QName
     * @return data child schema node
     * @throws IllegalArgumentException
     *             if the schema node does not allow children
     */
    @Nullable
    public static SchemaNode findDataChildSchemaByQName(final SchemaNode node, final QName qname) {
        SchemaNode child = null;
        if (node instanceof DataNodeContainer) {
            child = ((DataNodeContainer) node).getDataChildByName(qname);
            if (child == null && node instanceof SchemaContext) {
                child = tryFindRpc((SchemaContext) node, qname).orElse(null);
            }
            if (child == null && node instanceof NotificationNodeContainer) {
                child = tryFindNotification((NotificationNodeContainer) node, qname).orElse(null);
            }
            if (child == null && node instanceof ActionNodeContainer) {
                child = tryFindAction((ActionNodeContainer) node, qname).orElse(null);
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

    /**
     * Find child schema node identified by its QName within a provided schema node. This method performs lookup
     * in both the namespace of groupings and the namespace of all leafs, leaf-lists, lists, containers, choices, rpcs,
     * actions, notifications, anydatas and anyxmls according to RFC6050/RFC7950 section 6.2.1.
     *
     * <p>
     * This method is deprecated, because name conflicts can occur between the namespace of groupings and namespace
     * of data nodes and in consequence lookup could be ambiguous.
     *
     * @param node
     *            schema node
     * @param qname
     *            QName
     * @return child schema node
     * @throws IllegalArgumentException
     *             if the schema node does not allow children
     *
     * @deprecated Use {@link #findChildSchemaNodesByQName(SchemaNode, QName)} instead.
     */
    @Deprecated
    public static SchemaNode findChildSchemaByQName(final SchemaNode node, final QName qname) {
        SchemaNode child = findDataChildSchemaByQName(node, qname);
        if (child == null && node instanceof DataNodeContainer) {
            child = tryFindGroupings((DataNodeContainer) node, qname).orElse(null);
        }

        return child;
    }

    /**
     * Finds schema node for given path in schema context. This method performs lookup in both the namespace
     * of groupings and the namespace of all leafs, leaf-lists, lists, containers, choices, rpcs, actions,
     * notifications, anydatas and anyxmls according to Rfc6050/Rfc7950 section 6.2.1.
     *
     * <p>
     * This method returns collection of SchemaNodes, because name conflicts can occur between the namespace
     * of groupings and namespace of data nodes. This method finds and collects all schema nodes that matches supplied
     * SchemaPath and returns them all as collection of schema nodes.
     *
     * @param schemaContext
     *            schema context
     * @param path
     *            path
     * @return collection of schema nodes on path
     */
    public static Collection<SchemaNode> findParentSchemaNodesOnPath(final SchemaContext schemaContext,
            final SchemaPath path) {
        final Collection<SchemaNode> currentNodes = new ArrayList<>();
        final Collection<SchemaNode> childNodes = new ArrayList<>();
        currentNodes.add(Preconditions.checkNotNull(schemaContext));
        for (final QName qname : path.getPathFromRoot()) {
            for (final SchemaNode current : currentNodes) {
                childNodes.addAll(findChildSchemaNodesByQName(current, qname));
            }
            currentNodes.clear();
            currentNodes.addAll(childNodes);
            childNodes.clear();
        }

        return currentNodes;
    }

    /**
     * Find child schema node identified by its QName within a provided schema node. This method performs lookup in both
     * the namespace of groupings and the namespace of all leafs, leaf-lists, lists, containers, choices, rpcs,
     * actions, notifications, anydatas and anyxmls according to RFC6050/RFC7950 section 6.2.1.
     *
     * <p>
     * This method returns collection of SchemaNodes, because name conflicts can occur between the namespace
     * of groupings and namespace of data nodes. This method finds and collects all schema nodes with supplied QName
     * and returns them all as collection of schema nodes.
     *
     * @param node
     *            schema node
     * @param qname
     *            QName
     * @return collection of child schema nodes
     * @throws IllegalArgumentException
     *             if the schema node does not allow children
     */
    public static Collection<SchemaNode> findChildSchemaNodesByQName(final SchemaNode node, final QName qname) {
        final List<SchemaNode> childNodes = new ArrayList<>();
        final SchemaNode dataNode = findDataChildSchemaByQName(node, qname);
        if (dataNode != null) {
            childNodes.add(dataNode);
        }
        if (node instanceof DataNodeContainer) {
            tryFindGroupings((DataNodeContainer) node, qname).ifPresent(childNodes::add);
        }
        return childNodes.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(childNodes);
    }

    private static Optional<SchemaNode> tryFindGroupings(final DataNodeContainer dataNodeContainer, final QName qname) {
        return Optional
                .ofNullable(Iterables.find(dataNodeContainer.getGroupings(), new SchemaNodePredicate(qname), null));
    }

    private static Optional<SchemaNode> tryFindRpc(final SchemaContext ctx, final QName qname) {
        return Optional.ofNullable(Iterables.find(ctx.getOperations(), new SchemaNodePredicate(qname), null));
    }

    private static Optional<SchemaNode> tryFindNotification(final NotificationNodeContainer notificationContanier,
            final QName qname) {
        return Optional.ofNullable(
                Iterables.find(notificationContanier.getNotifications(), new SchemaNodePredicate(qname), null));
    }

    private static Optional<SchemaNode> tryFindAction(final ActionNodeContainer actionContanier, final QName qname) {
        return Optional.ofNullable(Iterables.find(actionContanier.getActions(), new SchemaNodePredicate(qname), null));
    }

    private static final class SchemaNodePredicate implements Predicate<SchemaNode> {
        private final QName qname;

        SchemaNodePredicate(final QName qname) {
            this.qname = qname;
        }

        @Override
        public boolean apply(final SchemaNode input) {
            return input.getQName().equals(qname);
        }
    }
}
