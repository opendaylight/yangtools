/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafRefValidatation {

    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidatation.class);
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final DataTreeCandidate tree;
    private final List<String> errorsMessages =  new LinkedList<>();
    private final Set<LeafRefContext> validatedLeafRefCtx =  new HashSet<>();

    private LeafRefValidatation(final DataTreeCandidate tree) {
        this.tree = tree;
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        new LeafRefValidatation(tree).validate0(rootLeafRefCtx);
    }
    private void validate0(final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {

        final DataTreeCandidateNode rootNode = tree.getRootNode();

        final Collection<DataTreeCandidateNode> childNodes = rootNode.getChildNodes();
        for (final DataTreeCandidateNode dataTreeCandidateNode : childNodes) {

            final ModificationType modificationType = dataTreeCandidateNode
                    .getModificationType();
            if (modificationType != ModificationType.UNMODIFIED) {

                final PathArgument identifier = dataTreeCandidateNode.getIdentifier();
                final QName childQName = identifier.getNodeType();

                final LeafRefContext referencedByCtx = rootLeafRefCtx
                        .getReferencedChildByName(childQName);
                final LeafRefContext referencingCtx = rootLeafRefCtx
                        .getReferencingChildByName(childQName);
                if (referencedByCtx != null || referencingCtx != null) {
                    final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier
                            .create(dataTreeCandidateNode.getIdentifier());
                    validateNode(dataTreeCandidateNode, referencedByCtx,
                            referencingCtx, yangInstanceIdentifier);
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
            throw new LeafRefDataValidationFailedException(message.toString(),
                    errCount);
        }

    }

    private void validateNode(final DataTreeCandidateNode node,
            final LeafRefContext referencedByCtx, final LeafRefContext referencingCtx,
            final YangInstanceIdentifier current) {

        if ((node.getModificationType() == ModificationType.WRITE)
                && node.getDataAfter().isPresent()) {
            final Optional<NormalizedNode<?, ?>> dataAfter = node.getDataAfter();
            final NormalizedNode<?, ?> normalizedNode = dataAfter.get();
            validateNodeData(normalizedNode, referencedByCtx, referencingCtx,
                    node.getModificationType(), current);
            return;
        }

        if (node.getModificationType() == ModificationType.DELETE
                && referencedByCtx != null) {
            final Optional<NormalizedNode<?, ?>> dataBefor = node.getDataBefore();
            final NormalizedNode<?, ?> normalizedNode = dataBefor.get();
            validateNodeData(normalizedNode, referencedByCtx, null,
                    node.getModificationType(), current);
            return;
        }

        final Collection<DataTreeCandidateNode> childNodes = node.getChildNodes();
        for (final DataTreeCandidateNode childNode : childNodes) {
            final ModificationType modificationType = childNode.getModificationType();

            if (modificationType != ModificationType.UNMODIFIED) {

                final LeafRefContext childReferencedByCtx = getReferencedByCtxChild(
                        referencedByCtx, childNode);
                final LeafRefContext childReferencingCtx = getReferencingCtxChild(
                        referencingCtx, childNode);

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(childNode.getIdentifier());
                    validateNode(childNode, childReferencedByCtx,
                            childReferencingCtx, childYangInstanceIdentifier);
                }
            }

        }

    }

    private static LeafRefContext getReferencingCtxChild(
            final LeafRefContext referencingCtx, final DataTreeCandidateNode childNode) {

        LeafRefContext childReferencingCtx = null;
        if (referencingCtx != null) {
            final PathArgument identifier = childNode.getIdentifier();
            final QName childQName = identifier.getNodeType();

            childReferencingCtx = referencingCtx
                    .getReferencingChildByName(childQName);

            if (childReferencingCtx == null) {
                final NormalizedNode<?, ?> data = childNode.getDataAfter().get();
                if (data instanceof MapEntryNode
                        || data instanceof UnkeyedListEntryNode) {
                    childReferencingCtx = referencingCtx;
                }
            }
        }

        return childReferencingCtx;
    }

    private static LeafRefContext getReferencedByCtxChild(
            final LeafRefContext referencedByCtx, final DataTreeCandidateNode childNode) {

        LeafRefContext childReferencedByCtx = null;
        if (referencedByCtx != null) {
            final PathArgument identifier = childNode.getIdentifier();
            final QName childQName = identifier.getNodeType();

            childReferencedByCtx = referencedByCtx
                    .getReferencedChildByName(childQName);
            if (childReferencedByCtx == null) {
                final NormalizedNode<?, ?> data = childNode.getDataAfter().get();
                if (data instanceof MapEntryNode
                        || data instanceof UnkeyedListEntryNode) {
                    childReferencedByCtx = referencedByCtx;
                }
            }
        }

        return childReferencedByCtx;
    }

    private void validateNodeData(final NormalizedNode<?, ?> node,
            final LeafRefContext referencedByCtx, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {

        if (node instanceof LeafNode) {
            final LeafNode<?> leaf = (LeafNode<?>) node;

            if (referencedByCtx != null && referencedByCtx.isReferenced()) {
                validateLeafRefTargetNodeData(leaf, referencedByCtx,
                        modificationType);
            }
            if (referencingCtx != null && referencingCtx.isReferencing()) {
                validateLeafRefNodeData(leaf, referencingCtx, modificationType,
                        current);
            }

            return;
        }

        if (node instanceof LeafSetNode) {
            final LeafSetNode<?> leafSet = (LeafSetNode<?>) node;

            if (referencedByCtx == null && referencingCtx == null) {
                return;
            }

            final Iterable<? extends NormalizedNode<?, ?>> leafSetEntries = leafSet
                    .getValue();
            for (final NormalizedNode<?, ?> leafSetEntry : leafSetEntries) {
                if (referencedByCtx != null && referencedByCtx.isReferenced()) {
                    validateLeafRefTargetNodeData(leafSetEntry,
                            referencedByCtx, modificationType);
                }
                if (referencingCtx != null && referencingCtx.isReferencing()) {
                    validateLeafRefNodeData(leafSetEntry, referencingCtx,
                            modificationType, current);
                }
            }

            return;
        }

        if (node instanceof ChoiceNode) {
            final ChoiceNode choice = (ChoiceNode) node;
            final Iterable<DataContainerChild<? extends PathArgument, ?>> childs = choice
                    .getValue();
            for (final DataContainerChild<? extends PathArgument, ?> dataContainerChild : childs) {
                final QName qname = dataContainerChild.getNodeType();

                LeafRefContext childReferencedByCtx = null;
                LeafRefContext childReferencingCtx = null;
                if (referencedByCtx != null) {
                    childReferencedByCtx = findReferencedByCtxUnderChoice(
                            referencedByCtx, qname);
                }
                if (referencingCtx != null) {
                    childReferencingCtx = findReferencingCtxUnderChoice(
                            referencingCtx, qname);
                }
                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType,
                            childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof DataContainerNode) {
            final DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            final Iterable<DataContainerChild<? extends PathArgument, ?>> dataContainerChilds = dataContainerNode
                    .getValue();

            for (final DataContainerChild<? extends PathArgument, ?> dataContainerChild : dataContainerChilds) {
                final QName qname = dataContainerChild.getNodeType();

                LeafRefContext childReferencedByCtx = null;
                LeafRefContext childReferencingCtx = null;
                if (referencedByCtx != null) {
                    childReferencedByCtx = referencedByCtx
                            .getReferencedChildByName(qname);
                }
                if (referencingCtx != null) {
                    childReferencingCtx = referencingCtx
                            .getReferencingChildByName(qname);
                }
                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    final YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType,
                            childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof MapNode) {
            final MapNode map = (MapNode) node;
            final Iterable<MapEntryNode> mapEntries = map.getValue();
            for (final MapEntryNode mapEntry : mapEntries) {
                final Iterable<DataContainerChild<? extends PathArgument, ?>> mapEntryNodes = mapEntry
                        .getValue();
                final YangInstanceIdentifier mapEntryYangInstanceIdentifier = current
                        .node(mapEntry.getIdentifier());
                for (final DataContainerChild<? extends PathArgument, ?> mapEntryNode : mapEntryNodes) {
                    final QName qname = mapEntryNode.getNodeType();

                    LeafRefContext childReferencedByCtx = null;
                    LeafRefContext childReferencingCtx = null;
                    if (referencedByCtx != null) {
                        childReferencedByCtx = referencedByCtx
                                .getReferencedChildByName(qname);
                    }
                    if (referencingCtx != null) {
                        childReferencingCtx = referencingCtx
                                .getReferencingChildByName(qname);
                    }
                    if (childReferencedByCtx != null
                            || childReferencingCtx != null) {
                        final YangInstanceIdentifier mapEntryNodeYangInstanceIdentifier = mapEntryYangInstanceIdentifier
                                .node(mapEntryNode.getIdentifier());
                        validateNodeData(mapEntryNode, childReferencedByCtx,
                                childReferencingCtx, modificationType,
                                mapEntryNodeYangInstanceIdentifier);
                    }
                }
            }

        }
        // FIXME if(node instance of UnkeyedListNode ...
    }

    private static LeafRefContext findReferencingCtxUnderChoice(
            final LeafRefContext referencingCtx, final QName qname) {

        final Map<QName, LeafRefContext> referencingChilds = referencingCtx
                .getReferencingChilds();
        final Set<Entry<QName, LeafRefContext>> childs = referencingChilds.entrySet();
        for (final Entry<QName, LeafRefContext> child : childs) {
            final LeafRefContext referencingChildByName = child.getValue()
                    .getReferencingChildByName(qname);
            if (referencingChildByName != null) {
                return referencingChildByName;
            }
        }

        return null;
    }

    private static LeafRefContext findReferencedByCtxUnderChoice(
            final LeafRefContext referencedByCtx, final QName qname) {

        final Map<QName, LeafRefContext> referencedByChilds = referencedByCtx
                .getReferencedByChilds();
        final Set<Entry<QName, LeafRefContext>> childs = referencedByChilds
                .entrySet();
        for (final Entry<QName, LeafRefContext> child : childs) {
            final LeafRefContext referencedByChildByName = child.getValue()
                    .getReferencedChildByName(qname);
            if (referencedByChildByName != null) {
                return referencedByChildByName;
            }
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    private void validateLeafRefTargetNodeData(final NormalizedNode<?, ?> leaf,
            final LeafRefContext referencedByCtx, final ModificationType modificationType) {

        final StringBuilder header_log = new StringBuilder();
        final StringBuilder log = new StringBuilder();
        header_log.append("Operation [" + modificationType
                + "] validate data of leafref TARGET node: name["
                + referencedByCtx.getNodeName() + "] = value["
                + leaf.getValue() + "]");

        if (validatedLeafRefCtx.contains(referencedByCtx)) {
            header_log.append(" -> SKIP: Already validated");
            LOG.debug(header_log.toString());
            return;
        }

        final Map<QName, LeafRefContext> allReferencedByLeafRefCtxs = referencedByCtx
                .getAllReferencedByLeafRefCtxs();

        final Map<LeafRefContext, Set> leafRefsValues = new HashMap<>();
        final Collection<LeafRefContext> leafrefs = allReferencedByLeafRefCtxs
                .values();
        for (final LeafRefContext leafRefContext : leafrefs) {
            if (leafRefContext.isReferencing()) {
                final Set<Object> values = new HashSet<>();

                final SchemaPath leafRefNodeSchemaPath = leafRefContext
                        .getCurrentNodePath();
                final LeafRefPath leafRefNodePath = LeafRefUtils
                        .schemaPathToLeafRefPath(leafRefNodeSchemaPath,
                                leafRefContext.getLeafRefContextModule());
                final Iterable<QNameWithPredicate> pathFromRoot = leafRefNodePath
                        .getPathFromRoot();
                addValues(values, tree.getRootNode().getDataAfter(),
                        pathFromRoot, null, QNameWithPredicate.ROOT);
                leafRefsValues.put(leafRefContext, values);
            }
        }

        final Set<Object> leafRefTargetNodeValues = new HashSet<>();
        final SchemaPath nodeSchemaPath = referencedByCtx.getCurrentNodePath();
        final LeafRefPath nodePath = LeafRefUtils.schemaPathToLeafRefPath(
                nodeSchemaPath, referencedByCtx.getLeafRefContextModule());
        addValues(leafRefTargetNodeValues, tree.getRootNode().getDataAfter(),
                nodePath.getPathFromRoot(), null, QNameWithPredicate.ROOT);

        boolean valid = true;
        final Set<Entry<LeafRefContext, Set>> entrySet = leafRefsValues
                .entrySet();
        for (final Entry<LeafRefContext, Set> entry : entrySet) {
            final LeafRefContext leafRefContext = entry.getKey();
            final Set leafRefValuesSet = entry.getValue();
            for (final Object leafRefsValue : leafRefValuesSet) {
                if (!leafRefTargetNodeValues.contains(leafRefsValue)) {

                    final StringBuilder sb = createInvalidTargetMessage(leaf,
                            leafRefTargetNodeValues, leafRefContext,
                            leafRefsValue);
                    log.append(NEW_LINE);
                    log.append(sb.toString());
                    log.append(FAILED);

                    sb.append(NEW_LINE);
                    errorsMessages.add(sb.toString());

                    valid = false;
                } else {
                    log.append(NEW_LINE);
                    log.append("Valid leafref value [");
                    log.append(leafRefsValue);
                    log.append("]");
                    log.append(SUCCESS);
                }
            }
        }

        header_log.append(valid ? SUCCESS : FAILED);
        LOG.debug(header_log.append(log.toString()).toString());

        validatedLeafRefCtx.add(referencedByCtx);
    }

    private static StringBuilder createInvalidTargetMessage(final NormalizedNode<?, ?> leaf,
            final Set<?> leafRefTargetNodeValues, final LeafRefContext leafRefContext,
            final Object leafRefsValue) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid leafref value [");
        sb.append(leafRefsValue);
        sb.append("] allowed values ");
        sb.append(leafRefTargetNodeValues);
        sb.append(" by validation of leafref TARGET node: ");
        sb.append(leaf.getNodeType());
        sb.append(" path of invalid LEAFREF node: ");
        sb.append(leafRefContext.getCurrentNodePath());
        sb.append(" leafRef target path: ");
        sb.append(leafRefContext.getAbsoluteLeafRefTargetPath());
        return sb;
    }

    private void validateLeafRefNodeData(final NormalizedNode<?, ?> leaf,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {

        final StringBuilder headerLog = new StringBuilder();
        final StringBuilder log = new StringBuilder();

        headerLog.append("Operation [");
        headerLog.append(modificationType);
        headerLog.append("] validate data of LEAFREF node: name[");
        headerLog.append(referencingCtx.getNodeName());
        headerLog.append("] = value[");
        headerLog.append(leaf.getValue());
        headerLog.append(']');

        final HashSet<Object> values = new HashSet<>();
        final LeafRefPath targetPath = referencingCtx.getAbsoluteLeafRefTargetPath();
        final Iterable<QNameWithPredicate> pathFromRoot = targetPath
                .getPathFromRoot();

        addValues(values, tree.getRootNode().getDataAfter(), pathFromRoot,
                current, QNameWithPredicate.ROOT);

        if (!values.contains(leaf.getValue())) {
            final StringBuilder sb = createInvalidLeafRefMessage(leaf,
                    referencingCtx, values);
            errorsMessages.add(sb.toString());

            headerLog.append(FAILED);
            log.append(sb.toString());
        } else {
            headerLog.append(SUCCESS);
        }

        LOG.debug(headerLog.toString());
        if (log.length() != 0) {
            LOG.debug(log.toString());
        }
    }

    private static StringBuilder createInvalidLeafRefMessage(
            final NormalizedNode<?, ?> leaf, final LeafRefContext referencingCtx,
            final Set<?> values) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid leafref value [");
        sb.append(leaf.getValue());
        sb.append("] allowed values ");
        sb.append(values);
        sb.append(" of LEAFREF node: ");
        sb.append(leaf.getNodeType());
        sb.append(" leafRef target path: ");
        sb.append(referencingCtx.getAbsoluteLeafRefTargetPath());
        sb.append(NEW_LINE);
        return sb;
    }

    private void addValues(final Set<Object> values,
            final Optional<? extends NormalizedNode<?, ?>> optDataNode,
            final Iterable<QNameWithPredicate> path, final YangInstanceIdentifier current,
            final QNameWithPredicate previousQName) {

        if (!optDataNode.isPresent()) {
            return;
        }
        final NormalizedNode<?, ?> node = optDataNode.get();

        if (node instanceof LeafNode || node instanceof LeafSetEntryNode) {
            values.add(node.getValue());
            return;
        } else if (node instanceof LeafSetNode<?>) {
            final LeafSetNode<?> leafSetNode = (LeafSetNode<?>) node;
            final Iterable<? extends NormalizedNode<?, ?>> entries = leafSetNode
                    .getValue();
            for (final NormalizedNode<?, ?> entry : entries) {
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
        final PathArgument pathArgument = toPathArgument(qName);

        if (node instanceof DataContainerNode) {
            final DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            final Optional<DataContainerChild<? extends PathArgument, ?>> child = dataContainerNode
                    .getChild(pathArgument);

            if (child.isPresent()) {
                addValues(values, child, nextLevel(path), current,
                        qnameWithPredicate);
            } else {
                final Iterable<ChoiceNode> choiceNodes = getChoiceNodes(dataContainerNode);
                for (final ChoiceNode choiceNode : choiceNodes) {
                    addValues(values, Optional.of(choiceNode), path, current,
                            qnameWithPredicate);
                }
            }

        } else if (node instanceof MapNode) {
            final MapNode map = (MapNode) node;
            final List<QNamePredicate> qNamePredicates = previousQName
                    .getQNamePredicates();
            if (qNamePredicates.isEmpty() || current == null) {
                final Iterable<MapEntryNode> value = map.getValue();
                for (final MapEntryNode mapEntryNode : value) {
                    final Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                            .getChild(pathArgument);

                    if (child.isPresent()) {
                        addValues(values, child, nextLevel(path), current,
                                qnameWithPredicate);
                    } else {
                        final Iterable<ChoiceNode> choiceNodes = getChoiceNodes(mapEntryNode);
                        for (final ChoiceNode choiceNode : choiceNodes) {
                            addValues(values, Optional.of(choiceNode), path,
                                    current, qnameWithPredicate);
                        }
                    }
                }
            } else {
                final Map<QName, Set<?>> keyValues = new HashMap<>();

                final Iterator<QNamePredicate> predicates = qNamePredicates
                        .iterator();
                while (predicates.hasNext()) {
                    final QNamePredicate predicate = predicates.next();
                    final QName identifier = predicate.getIdentifier();
                    final LeafRefPath predicatePathKeyExpression = predicate
                            .getPathKeyExpression();

                    final Set<?> pathKeyExprValues = getPathKeyExpressionValues(
                            predicatePathKeyExpression, current);

                    keyValues.put(identifier, pathKeyExprValues);
                }

                final Iterable<MapEntryNode> mapEntryNodes = map.getValue();
                for (final MapEntryNode mapEntryNode : mapEntryNodes) {
                    if (isMatchingPredicate(mapEntryNode, keyValues)) {
                        final Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                                .getChild(pathArgument);

                        if (child.isPresent()) {
                            addValues(values, child, nextLevel(path), current,
                                    qnameWithPredicate);
                        } else {
                            final Iterable<ChoiceNode> choiceNodes = getChoiceNodes(mapEntryNode);
                            for (final ChoiceNode choiceNode : choiceNodes) {
                                addValues(values, Optional.of(choiceNode),
                                        path, current, qnameWithPredicate);
                            }
                        }
                    }
                }

            }
        }
    }

    private static Iterable<ChoiceNode> getChoiceNodes(final DataContainerNode<?> dataContainerNode) {

        final LinkedList<ChoiceNode> choiceNodes = new LinkedList<ChoiceNode>();

        final Iterable<DataContainerChild<? extends PathArgument, ?>> childs = dataContainerNode
                .getValue();
        for (final DataContainerChild<? extends PathArgument, ?> child : childs) {
            if (child instanceof ChoiceNode) {
                choiceNodes.add((ChoiceNode) child);
            }
        }
        return choiceNodes;
    }

    private static boolean isMatchingPredicate(final MapEntryNode mapEntryNode,
            final Map<QName, Set<?>> allowedKeyValues) {

        final NodeIdentifierWithPredicates identifier = mapEntryNode.getIdentifier();
        final Map<QName, Object> entryKeyValues = identifier.getKeyValues();

        final Set<Entry<QName, Object>> entryKeyValueSet = entryKeyValues.entrySet();
        for (final Entry<QName, Object> entryKeyValue : entryKeyValueSet) {
            final QName key = entryKeyValue.getKey();
            final Object value = entryKeyValue.getValue();

            final Set<?> allowedValues = allowedKeyValues.get(key);
            if (allowedValues != null && !allowedValues.contains(value)) {
                return false;
            }

        }

        return true;
    }

    private Set<?> getPathKeyExpressionValues(
            final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {

        final Optional<NormalizedNode<?, ?>> parent = findParentNode(tree
                .getRootNode().getDataAfter(), current);

        final Iterable<QNameWithPredicate> predicatePathExpr = predicatePathKeyExpression
                .getPathFromRoot();
        final Iterable<QNameWithPredicate> predicatePath = nextLevel(predicatePathExpr);

        final Set<Object> values = new HashSet<>();
        if (parent != null) {
            addValues(values, parent, predicatePath, null,
                    QNameWithPredicate.ROOT);
        }

        return values;
    }

    private static Optional<NormalizedNode<?, ?>> findParentNode(
            final Optional<NormalizedNode<?, ?>> root, final YangInstanceIdentifier path) {
        Optional<NormalizedNode<?, ?>> currentNode = root;
        final Iterator<PathArgument> pathIterator = path.getPathArguments()
                .iterator();
        while (pathIterator.hasNext()) {
            final PathArgument childPathArgument = pathIterator.next();
            if (pathIterator.hasNext() && currentNode.isPresent()) {
                currentNode = NormalizedNodes.getDirectChild(currentNode.get(),
                        childPathArgument);
            } else {
                return currentNode;
            }
        }
        return Optional.absent();
    }

    private static Iterable<QNameWithPredicate> nextLevel(final Iterable<QNameWithPredicate> path) {
        return Iterables.skip(path, 1);
    }

    private static PathArgument toPathArgument(final QName qName) {
        return YangInstanceIdentifier.of(qName).getLastPathArgument();
    }
}
