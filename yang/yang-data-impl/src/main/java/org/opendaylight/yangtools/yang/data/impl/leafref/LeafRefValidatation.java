/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafRefValidatation {

    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidatation.class);
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final Set<LeafRefContext> validatedLeafRefCtx = new HashSet<>();
    private final List<String> errorsMessages = new ArrayList<>();
    private final DataTreeCandidate tree;

    private LeafRefValidatation(final DataTreeCandidate tree) {
        this.tree = tree;
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        new LeafRefValidatation(tree).validate0(rootLeafRefCtx);
    }

    private void validate0(final LeafRefContext rootLeafRefCtx) throws LeafRefDataValidationFailedException {
        for (final DataTreeCandidateNode dataTreeCandidateNode : tree.getRootNode().getChildNodes()) {
            if (dataTreeCandidateNode.getModificationType() != ModificationType.UNMODIFIED) {
                final PathArgument identifier = dataTreeCandidateNode.getIdentifier();
                final QName childQName = identifier.getNodeType();

                final LeafRefContext referencedByCtx = rootLeafRefCtx.getReferencedChildByName(childQName);
                final LeafRefContext referencingCtx = rootLeafRefCtx.getReferencingChildByName(childQName);
                if (referencedByCtx != null || referencingCtx != null) {
                    final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier
                            .create(dataTreeCandidateNode.getIdentifier());
                    validateNode(dataTreeCandidateNode, referencedByCtx, referencingCtx, yangInstanceIdentifier);
                }
            }
        }

        if (!errorsMessages.isEmpty()) {
            final StringBuilder message = new StringBuilder();
            int errCount = 0;
            for (final String errorMessage : errorsMessages) {
                message.append(errorMessage);
                errCount++;
            }
            throw new LeafRefDataValidationFailedException(message.toString(), errCount);
        }
    }

    private void validateNode(final DataTreeCandidateNode node, final LeafRefContext referencedByCtx,
        final LeafRefContext referencingCtx, final YangInstanceIdentifier current) {

        if (node.getModificationType() == ModificationType.WRITE && node.getDataAfter().isPresent()) {
            final Optional<NormalizedNode<?, ?>> dataAfter = node.getDataAfter();
            final NormalizedNode<?, ?> normalizedNode = dataAfter.get();
            validateNodeData(normalizedNode, referencedByCtx, referencingCtx,
                    node.getModificationType(), current);
            return;
        }

        if (node.getModificationType() == ModificationType.DELETE && referencedByCtx != null) {
            final Optional<NormalizedNode<?, ?>> dataBefor = node.getDataBefore();
            final NormalizedNode<?, ?> normalizedNode = dataBefor.get();
            validateNodeData(normalizedNode, referencedByCtx, null,
                    node.getModificationType(), current);
            return;
        }

        final Collection<DataTreeCandidateNode> childNodes = node.getChildNodes();
        for (final DataTreeCandidateNode childNode : childNodes) {
            if (childNode.getModificationType() != ModificationType.UNMODIFIED) {
                final LeafRefContext childReferencedByCtx = getReferencedByCtxChild(referencedByCtx, childNode);
                final LeafRefContext childReferencingCtx = getReferencingCtxChild(referencingCtx, childNode);

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current.node(childNode.getIdentifier());
                    validateNode(childNode, childReferencedByCtx,childReferencingCtx, childYangInstanceIdentifier);
                }
            }
        }
    }

    private static LeafRefContext getReferencingCtxChild(final LeafRefContext referencingCtx,
            final DataTreeCandidateNode childNode) {
        if (referencingCtx == null) {
            return null;
        }

        final QName childQName = childNode.getIdentifier().getNodeType();
        LeafRefContext childReferencingCtx = referencingCtx.getReferencingChildByName(childQName);
        if (childReferencingCtx == null) {
            final NormalizedNode<?, ?> data = childNode.getDataAfter().get();
            if (data instanceof MapEntryNode || data instanceof UnkeyedListEntryNode) {
                childReferencingCtx = referencingCtx;
            }
        }

        return childReferencingCtx;
    }

    private static LeafRefContext getReferencedByCtxChild(final LeafRefContext referencedByCtx,
            final DataTreeCandidateNode childNode) {
        if (referencedByCtx == null) {
            return null;
        }

        final QName childQName = childNode.getIdentifier().getNodeType();
        LeafRefContext childReferencedByCtx = referencedByCtx.getReferencedChildByName(childQName);
        if (childReferencedByCtx == null) {
            final NormalizedNode<?, ?> data = childNode.getDataAfter().get();
            if (data instanceof MapEntryNode || data instanceof UnkeyedListEntryNode) {
                childReferencedByCtx = referencedByCtx;
            }
        }

        return childReferencedByCtx;
    }

    private void validateNodeData(final NormalizedNode<?, ?> node, final LeafRefContext referencedByCtx, final
            LeafRefContext referencingCtx, final ModificationType modificationType, final YangInstanceIdentifier current) {

        if (node instanceof LeafNode) {
            final LeafNode<?> leaf = (LeafNode<?>) node;

            if (referencedByCtx != null && referencedByCtx.isReferenced()) {
                validateLeafRefTargetNodeData(leaf, referencedByCtx, modificationType);
            }
            if (referencingCtx != null && referencingCtx.isReferencing()) {
                validateLeafRefNodeData(leaf, referencingCtx, modificationType, current);
            }

            return;
        }

        if (node instanceof LeafSetNode) {
            if (referencedByCtx == null && referencingCtx == null) {
                return;
            }

            final LeafSetNode<?> leafSet = (LeafSetNode<?>) node;
            for (final NormalizedNode<?, ?> leafSetEntry : leafSet.getValue()) {
                if (referencedByCtx != null && referencedByCtx.isReferenced()) {
                    validateLeafRefTargetNodeData(leafSetEntry, referencedByCtx, modificationType);
                }
                if (referencingCtx != null && referencingCtx.isReferencing()) {
                    validateLeafRefNodeData(leafSetEntry, referencingCtx, modificationType, current);
                }
            }

            return;
        }

        if (node instanceof ChoiceNode) {
            final ChoiceNode choice = (ChoiceNode) node;
            for (final DataContainerChild<? extends PathArgument, ?> dataContainerChild : choice.getValue()) {
                final QName qname = dataContainerChild.getNodeType();

                final LeafRefContext childReferencedByCtx;
                if (referencedByCtx != null) {
                    childReferencedByCtx = findReferencedByCtxUnderChoice(referencedByCtx, qname);
                } else {
                    childReferencedByCtx = null;
                }

                final LeafRefContext childReferencingCtx;
                if (referencingCtx != null) {
                    childReferencingCtx = findReferencingCtxUnderChoice(referencingCtx, qname);
                } else {
                    childReferencingCtx = null;
                }

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType, childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof DataContainerNode) {
            final DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;

            for (final DataContainerChild<? extends PathArgument, ?> dataContainerChild : dataContainerNode.getValue()) {
                final QName qname = dataContainerChild.getNodeType();

                final LeafRefContext childReferencedByCtx;
                if (referencedByCtx != null) {
                    childReferencedByCtx = referencedByCtx.getReferencedChildByName(qname);
                } else {
                    childReferencedByCtx = null;
                }

                final LeafRefContext childReferencingCtx;
                if (referencingCtx != null) {
                    childReferencingCtx = referencingCtx.getReferencingChildByName(qname);
                } else {
                    childReferencingCtx = null;
                }

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType, childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof MapNode) {
            final MapNode map = (MapNode) node;

            for (final MapEntryNode mapEntry : map.getValue()) {
                final YangInstanceIdentifier mapEntryYangInstanceIdentifier = current.node(mapEntry.getIdentifier());
                for (final DataContainerChild<? extends PathArgument, ?> mapEntryNode : mapEntry.getValue()) {
                    final QName qname = mapEntryNode.getNodeType();

                    final LeafRefContext childReferencedByCtx;
                    if (referencedByCtx != null) {
                        childReferencedByCtx = referencedByCtx.getReferencedChildByName(qname);
                    } else {
                        childReferencedByCtx = null;
                    }

                    final LeafRefContext childReferencingCtx;
                    if (referencingCtx != null) {
                        childReferencingCtx = referencingCtx.getReferencingChildByName(qname);
                    } else {
                        childReferencingCtx = null;
                    }

                    if (childReferencedByCtx != null || childReferencingCtx != null) {
                        final YangInstanceIdentifier mapEntryNodeYangInstanceIdentifier = mapEntryYangInstanceIdentifier
                                .node(mapEntryNode.getIdentifier());
                        validateNodeData(mapEntryNode, childReferencedByCtx,
                                childReferencingCtx, modificationType,
                                mapEntryNodeYangInstanceIdentifier);
                    }
                }
            }
        }
        // FIXME if (node instance of UnkeyedListNode ...
    }

    private static LeafRefContext findReferencingCtxUnderChoice(
            final LeafRefContext referencingCtx, final QName qname) {

        for (final LeafRefContext child : referencingCtx.getReferencingChilds().values()) {
            final LeafRefContext referencingChildByName = child.getReferencingChildByName(qname);
            if (referencingChildByName != null) {
                return referencingChildByName;
            }
        }

        return null;
    }

    private static LeafRefContext findReferencedByCtxUnderChoice(
            final LeafRefContext referencedByCtx, final QName qname) {

        for (final LeafRefContext child : referencedByCtx.getReferencedByChilds().values()) {
            final LeafRefContext referencedByChildByName = child.getReferencedChildByName(qname);
            if (referencedByChildByName != null) {
                return referencedByChildByName;
            }
        }

        return null;
    }

    private void validateLeafRefTargetNodeData(final NormalizedNode<?, ?> leaf, final LeafRefContext
            referencedByCtx, final ModificationType modificationType) {
        final Map<LeafRefContext, Set<?>> leafRefsValues = new HashMap<>();
        if (validatedLeafRefCtx.contains(referencedByCtx)) {
            leafRefTargetNodeDataLog(leaf, referencedByCtx, modificationType, leafRefsValues, null);
            return;
        }

        final Map<QName, LeafRefContext> allReferencedByLeafRefCtxs = referencedByCtx.getAllReferencedByLeafRefCtxs();
        for (final LeafRefContext leafRefContext : allReferencedByLeafRefCtxs.values()) {
            if (leafRefContext.isReferencing()) {
                final Set<Object> values = new HashSet<>();

                final SchemaPath leafRefNodeSchemaPath = leafRefContext.getCurrentNodePath();
                final LeafRefPath leafRefNodePath = LeafRefUtils.schemaPathToLeafRefPath(leafRefNodeSchemaPath,
                                leafRefContext.getLeafRefContextModule());
                final Iterable<QNameWithPredicate> pathFromRoot = leafRefNodePath.getPathFromRoot();
                addValues(values, tree.getRootNode().getDataAfter(), pathFromRoot, null, QNameWithPredicate.ROOT);
                leafRefsValues.put(leafRefContext, values);
            }
        }

        if (!leafRefsValues.isEmpty()) {
            final Set<Object> leafRefTargetNodeValues = new HashSet<>();
            final SchemaPath nodeSchemaPath = referencedByCtx.getCurrentNodePath();
            final LeafRefPath nodePath = LeafRefUtils.schemaPathToLeafRefPath(nodeSchemaPath, referencedByCtx
                    .getLeafRefContextModule());
            addValues(leafRefTargetNodeValues, tree.getRootNode().getDataAfter(), nodePath.getPathFromRoot(), null,
                    QNameWithPredicate.ROOT);
            leafRefTargetNodeDataLog(leaf, referencedByCtx, modificationType, leafRefsValues,
                    leafRefTargetNodeValues);
        } else {
            leafRefTargetNodeDataLog(leaf, referencedByCtx, modificationType, null, null);
        }
        validatedLeafRefCtx.add(referencedByCtx);
    }

    private void leafRefTargetNodeDataLog(final NormalizedNode<?, ?> leaf, final LeafRefContext referencedByCtx,
            final ModificationType modificationType, final Map<LeafRefContext, Set<?>> leafRefsValues,
            final Set<Object> leafRefTargetNodeValues) {

        if (leafRefsValues != null && !leafRefsValues.isEmpty()) {
            final Set<Entry<LeafRefContext, Set<?>>> entrySet = leafRefsValues.entrySet();
            LOG.debug("Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}]",
                    modificationType, referencedByCtx.getNodeName(), leaf.getValue());
            for (final Entry<LeafRefContext, Set<?>> entry : entrySet) {
                final LeafRefContext leafRefContext = entry.getKey();
                final Set<?> leafRefValuesSet = entry.getValue();
                for (final Object leafRefsValue : leafRefValuesSet) {
                    if (leafRefTargetNodeValues != null && !leafRefTargetNodeValues.contains(leafRefsValue)) {
                        LOG.debug("Invalid leafref value [{}] allowed values {} by validation of leafref TARGET node:" +
                                " {} path of invalid LEAFREF node: {} leafRef target path: {} {}", leafRefsValue,
                                leafRefTargetNodeValues, leaf.getNodeType(), leafRefContext.getCurrentNodePath(),
                                leafRefContext.getAbsoluteLeafRefTargetPath(), FAILED);
                        errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s by validation " +
                                        "of  leafref TARGET node: %s path of invalid LEAFREF node: %s leafRef target " +
                                        "path: %s %s", leafRefsValue, leafRefTargetNodeValues, leaf.getNodeType(),
                                leafRefContext.getCurrentNodePath(), leafRefContext.getAbsoluteLeafRefTargetPath(),
                                FAILED));
                    } else {
                        LOG.debug("Valid leafref value [{}] {}", leafRefsValue, SUCCESS);
                    }
                }
            }
        } else if (leafRefsValues != null) {
            LOG.debug("Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}] -> SKIP: Already validated",
                    modificationType, referencedByCtx.getNodeName(), leaf.getValue());
        }
    }

    private void validateLeafRefNodeData(final NormalizedNode<?, ?> leaf, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {
        final HashSet<Object> values = new HashSet<>();
        final LeafRefPath targetPath = referencingCtx.getAbsoluteLeafRefTargetPath();
        final Iterable<QNameWithPredicate> pathFromRoot = targetPath.getPathFromRoot();

        addValues(values, tree.getRootNode().getDataAfter(), pathFromRoot, current, QNameWithPredicate.ROOT);

        if (!values.contains(leaf.getValue())) {
            LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}",
                    modificationType, referencingCtx.getNodeName(), leaf.getValue(), FAILED);
            LOG.debug("Invalid leafref value [{}] allowed values {} of LEAFREF node: {} leafRef target path: {}",
                    leaf.getValue(), values, leaf.getNodeType(), referencingCtx.getAbsoluteLeafRefTargetPath());
            errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s of LEAFREF node: %s " +
                            "leafRef  target path: %s", leaf.getValue(), values, leaf.getNodeType(), referencingCtx
                    .getAbsoluteLeafRefTargetPath()));
        } else {
            LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}", modificationType,
                    referencingCtx.getNodeName(), leaf.getValue(), SUCCESS);
        }
    }

    private void addValues(final Set<Object> values, final Optional<? extends NormalizedNode<?, ?>> optDataNode,
            final Iterable<QNameWithPredicate> path, final YangInstanceIdentifier current, final QNameWithPredicate previousQName) {

        if (!optDataNode.isPresent()) {
            return;
        }
        final NormalizedNode<?, ?> node = optDataNode.get();
        if (node instanceof ValueNode) {
            values.add(node.getValue());
            return;
        }
        if (node instanceof LeafSetNode<?>) {
            for (final NormalizedNode<?, ?> entry : ((LeafSetNode<?>) node).getValue()) {
                values.add(entry.getValue());
            }
            return;
        }

        final Iterator<QNameWithPredicate> iterator = path.iterator();
        if (!iterator.hasNext()) {
            return;
        }
        final QNameWithPredicate qnameWithPredicate = iterator.next();
        final QName qName = qnameWithPredicate.getQName();
        final PathArgument pathArgument = new NodeIdentifier(qName);

        if (node instanceof DataContainerNode) {
            final DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            final Optional<DataContainerChild<? extends PathArgument, ?>> child = dataContainerNode
                    .getChild(pathArgument);

            if (child.isPresent()) {
                addValues(values, child, nextLevel(path), current, qnameWithPredicate);
            } else {
                for (final ChoiceNode choiceNode : getChoiceNodes(dataContainerNode)) {
                    addValues(values, Optional.of(choiceNode), path, current,
                            qnameWithPredicate);
                }
            }

        } else if (node instanceof MapNode) {
            final MapNode map = (MapNode) node;
            final List<QNamePredicate> qNamePredicates = previousQName.getQNamePredicates();
            if (qNamePredicates.isEmpty() || current == null) {
                final Iterable<MapEntryNode> value = map.getValue();
                for (final MapEntryNode mapEntryNode : value) {
                    final Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                            .getChild(pathArgument);

                    if (child.isPresent()) {
                        addValues(values, child, nextLevel(path), current, qnameWithPredicate);
                    } else {
                        for (final ChoiceNode choiceNode : getChoiceNodes(mapEntryNode)) {
                            addValues(values, Optional.of(choiceNode), path, current, qnameWithPredicate);
                        }
                    }
                }
            } else {
                final Map<QName, Set<?>> keyValues = new HashMap<>();

                final Iterator<QNamePredicate> predicates = qNamePredicates.iterator();
                while (predicates.hasNext()) {
                    final QNamePredicate predicate = predicates.next();
                    final QName identifier = predicate.getIdentifier();
                    final LeafRefPath predicatePathKeyExpression = predicate
                            .getPathKeyExpression();

                    final Set<?> pathKeyExprValues = getPathKeyExpressionValues(
                            predicatePathKeyExpression, current);

                    keyValues.put(identifier, pathKeyExprValues);
                }

                for (final MapEntryNode mapEntryNode : map.getValue()) {
                    if (isMatchingPredicate(mapEntryNode, keyValues)) {
                        final Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                                .getChild(pathArgument);

                        if (child.isPresent()) {
                            addValues(values, child, nextLevel(path), current, qnameWithPredicate);
                        } else {
                            for (final ChoiceNode choiceNode : getChoiceNodes(mapEntryNode)) {
                                addValues(values, Optional.of(choiceNode),  path, current, qnameWithPredicate);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Iterable<ChoiceNode> getChoiceNodes(final DataContainerNode<?> dataContainerNode) {
        final List<ChoiceNode> choiceNodes = new ArrayList<>();
        for (final DataContainerChild<? extends PathArgument, ?> child : dataContainerNode.getValue()) {
            if (child instanceof ChoiceNode) {
                choiceNodes.add((ChoiceNode) child);
            }
        }
        return choiceNodes;
    }

    private static boolean isMatchingPredicate(final MapEntryNode mapEntryNode, final Map<QName, Set<?>> allowedKeyValues) {
        for (final Entry<QName, Object> entryKeyValue : mapEntryNode.getIdentifier().getKeyValues().entrySet()) {
            final Set<?> allowedValues = allowedKeyValues.get(entryKeyValue.getKey());
            if (allowedValues != null && !allowedValues.contains(entryKeyValue.getValue())) {
                return false;
            }
        }

        return true;
    }

    private Set<?> getPathKeyExpressionValues(final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {

        final Optional<NormalizedNode<?, ?>> parent = findParentNode(tree.getRootNode().getDataAfter(), current);

        final Iterable<QNameWithPredicate> predicatePathExpr = predicatePathKeyExpression.getPathFromRoot();
        final Iterable<QNameWithPredicate> predicatePath = nextLevel(predicatePathExpr);

        final Set<Object> values = new HashSet<>();
        if (parent != null) {
            addValues(values, parent, predicatePath, null,QNameWithPredicate.ROOT);
        }

        return values;
    }

    private static Optional<NormalizedNode<?, ?>> findParentNode(
            final Optional<NormalizedNode<?, ?>> root, final YangInstanceIdentifier path) {
        Optional<NormalizedNode<?, ?>> currentNode = root;
        final Iterator<PathArgument> pathIterator = path.getPathArguments().iterator();
        while (pathIterator.hasNext()) {
            final PathArgument childPathArgument = pathIterator.next();
            if (pathIterator.hasNext() && currentNode.isPresent()) {
                currentNode = NormalizedNodes.getDirectChild(currentNode.get(), childPathArgument);
            } else {
                return currentNode;
            }
        }
        return Optional.absent();
    }

    private static Iterable<QNameWithPredicate> nextLevel(final Iterable<QNameWithPredicate> path) {
        return Iterables.skip(path, 1);
    }
}