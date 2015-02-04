/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree.validation;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.leafrefcontext.utils.LeafRefUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yangtools.leafrefcontext.api.QNamePredicate;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import java.util.Iterator;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.Collection;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

@SuppressWarnings({ "rawtypes" })
public class DataTreeCandidateValidator {

    private static final Logger LOG = LoggerFactory.getLogger("");
    private static final String NEW_LINE = System.getProperty("line.separator");

    private DataTreeCandidate tree;
    private LinkedList<String> errorsMessages;
    private HashSet<LeafRefContext> validatedLeafRefCtx;

    public void validateLeafRefs(DataTreeCandidate tree,
            LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {

        this.tree = tree;
        this.errorsMessages = new LinkedList<String>();
        this.validatedLeafRefCtx = new HashSet<LeafRefContext>();

        DataTreeCandidateNode rootNode = tree.getRootNode();

        Collection<DataTreeCandidateNode> childNodes = rootNode.getChildNodes();
        for (DataTreeCandidateNode dataTreeCandidateNode : childNodes) {

            ModificationType modificationType = dataTreeCandidateNode
                    .getModificationType();
            if (modificationType != ModificationType.UNMODIFIED) {

                PathArgument identifier = dataTreeCandidateNode.getIdentifier();
                QName childQName = identifier.getNodeType();

                LeafRefContext referencedByCtx = rootLeafRefCtx
                        .getReferencedByChildByName(childQName);
                LeafRefContext referencingCtx = rootLeafRefCtx
                        .getReferencingChildByName(childQName);
                if (referencedByCtx != null || referencingCtx != null) {
                    YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier
                            .create(dataTreeCandidateNode.getIdentifier());
                    validateNode(dataTreeCandidateNode, referencedByCtx,
                            referencingCtx, yangInstanceIdentifier);
                }
            }

        }

        if (!errorsMessages.isEmpty()) {
            StringBuilder message = new StringBuilder();
            int errCount = 0;
            for (String errorMessage : errorsMessages) {
                message.append(errorMessage);
                errCount++;
            }
            throw new LeafRefDataValidationFailedException(message.toString(),
                    errCount);
        }

    }

    private void validateNode(DataTreeCandidateNode node,
            LeafRefContext referencedByCtx, LeafRefContext referencingCtx,
            YangInstanceIdentifier current) {

        if ((node.getModificationType() == ModificationType.WRITE || node
                .getModificationType() == ModificationType.MERGE)
                && node.getDataAfter().isPresent()) {

            Optional<NormalizedNode<?, ?>> dataAfter = node.getDataAfter();
            NormalizedNode<?, ?> normalizedNode = dataAfter.get();
            validateNodeData(normalizedNode, referencedByCtx, referencingCtx,
                    node.getModificationType(), current);
        } else if (node.getModificationType() == ModificationType.DELETE
                && referencedByCtx != null) {

            Optional<NormalizedNode<?, ?>> dataBefor = node.getDataBefore();
            NormalizedNode<?, ?> normalizedNode = dataBefor.get();
            validateNodeData(normalizedNode, referencedByCtx, null,
                    node.getModificationType(), current);
        }

        Collection<DataTreeCandidateNode> childNodes = node.getChildNodes();
        for (DataTreeCandidateNode dataTreeCandidateNode : childNodes) {

            ModificationType modificationType = dataTreeCandidateNode
                    .getModificationType();
            if (modificationType != ModificationType.UNMODIFIED) {

                PathArgument identifier = dataTreeCandidateNode.getIdentifier();
                QName childQName = identifier.getNodeType();

                LeafRefContext childReferencedByCtx = null;
                LeafRefContext childReferencingCtx = null;
                if (referencedByCtx != null) {
                    childReferencedByCtx = referencedByCtx
                            .getReferencedByChildByName(childQName);
                }
                if (referencingCtx != null) {
                    childReferencingCtx = referencingCtx
                            .getReferencingChildByName(childQName);
                }
                if (referencedByCtx != null || referencingCtx != null) {
                    YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(node.getIdentifier());
                    validateNode(dataTreeCandidateNode, childReferencedByCtx,
                            childReferencingCtx, childYangInstanceIdentifier);
                }
            }

        }

    }

    private void validateNodeData(NormalizedNode<?, ?> node,
            LeafRefContext referencedByCtx, LeafRefContext referencingCtx,
            ModificationType modificationType, YangInstanceIdentifier current) {

        if (node instanceof LeafNode) {
            LeafNode<?> leaf = (LeafNode<?>) node;

            if (referencedByCtx != null && referencedByCtx.isReferencedBy()) {
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
            LeafSetNode<?> leafSet = (LeafSetNode<?>) node;

            if (referencedByCtx == null && referencingCtx == null) {
                return;
            }

            Iterable<? extends NormalizedNode<?, ?>> leafSetEntries = leafSet
                    .getValue();
            for (NormalizedNode<?, ?> leafSetEntry : leafSetEntries) {
                if (referencedByCtx != null && referencedByCtx.isReferencedBy()) {
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
            ChoiceNode choice = (ChoiceNode) node;
            Iterable<DataContainerChild<? extends PathArgument, ?>> childs = choice
                    .getValue();
            for (DataContainerChild<? extends PathArgument, ?> dataContainerChild : childs) {
                QName qname = dataContainerChild.getNodeType();

                LeafRefContext childReferencedByCtx = null;
                LeafRefContext childReferencingCtx = null;
                if (referencedByCtx != null) {
                    childReferencedByCtx = findReferencedByCtxUnderChoice(referencedByCtx,qname);
                }
                if (referencingCtx != null) {
                    childReferencingCtx = findReferencingCtxUnderChoice(referencingCtx,qname);
                }
                if (referencedByCtx != null || referencingCtx != null) {
                    YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType,
                            childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof DataContainerNode) {
            DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            Iterable<DataContainerChild<? extends PathArgument, ?>> dataContainerChilds = dataContainerNode
                    .getValue();

            for (DataContainerChild<? extends PathArgument, ?> dataContainerChild : dataContainerChilds) {
                QName qname = dataContainerChild.getNodeType();

                LeafRefContext childReferencedByCtx = null;
                LeafRefContext childReferencingCtx = null;
                if (referencedByCtx != null) {
                    childReferencedByCtx = referencedByCtx
                            .getReferencedByChildByName(qname);
                }
                if (referencingCtx != null) {
                    childReferencingCtx = referencingCtx
                            .getReferencingChildByName(qname);
                }
                if (referencedByCtx != null || referencingCtx != null) {
                    YangInstanceIdentifier childYangInstanceIdentifier = current
                            .node(dataContainerChild.getIdentifier());
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType,
                            childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof MapNode) {
            MapNode map = (MapNode) node;
            Iterable<MapEntryNode> mapEntries = map.getValue();
            for (MapEntryNode mapEntry : mapEntries) {
                Iterable<DataContainerChild<? extends PathArgument, ?>> mapEntryNodes = mapEntry
                        .getValue();
                YangInstanceIdentifier mapEntryYangInstanceIdentifier = current
                        .node(mapEntry.getIdentifier());
                for (DataContainerChild<? extends PathArgument, ?> mapEntryNode : mapEntryNodes) {
                    QName qname = mapEntryNode.getNodeType();

                    LeafRefContext childReferencedByCtx = null;
                    LeafRefContext childReferencingCtx = null;
                    if (referencedByCtx != null) {
                        childReferencedByCtx = referencedByCtx
                                .getReferencedByChildByName(qname);
                    }
                    if (referencingCtx != null) {
                        childReferencingCtx = referencingCtx
                                .getReferencingChildByName(qname);
                    }
                    if (referencedByCtx != null || referencingCtx != null) {
                        YangInstanceIdentifier mapEntryNodeYangInstanceIdentifier = mapEntryYangInstanceIdentifier
                                .node(mapEntryNode.getIdentifier());
                        validateNodeData(mapEntryNode, childReferencedByCtx,
                                childReferencingCtx, modificationType,
                                mapEntryNodeYangInstanceIdentifier);
                    }
                }
            }

        }

        // :TODO if(node instance of UnkeyedListNode ...
    }

    private LeafRefContext findReferencingCtxUnderChoice(
            LeafRefContext referencingCtx, QName qname) {

        Map<QName, LeafRefContext> referencingChilds = referencingCtx
                .getReferencingChilds();
        Set<Entry<QName, LeafRefContext>> childs = referencingChilds.entrySet();
        for (Entry<QName, LeafRefContext> child : childs) {
            LeafRefContext referencingChildByName = child.getValue()
                    .getReferencingChildByName(qname);
            if (referencingChildByName != null) {
                return referencingChildByName;
            }
        }

        return null;
    }

    private LeafRefContext findReferencedByCtxUnderChoice(
            LeafRefContext referencedByCtx, QName qname) {

        Map<QName, LeafRefContext> referencedByChilds = referencedByCtx
                .getReferencedByChilds();
        Set<Entry<QName, LeafRefContext>> childs = referencedByChilds.entrySet();
        for (Entry<QName, LeafRefContext> child : childs) {
            LeafRefContext referencedByChildByName = child.getValue()
                    .getReferencedByChildByName(qname);
            if (referencedByChildByName != null) {
                return referencedByChildByName;
            }
        }

        return null;
    }

    private void validateLeafRefTargetNodeData(NormalizedNode<?, ?> leaf,
            LeafRefContext referencedByCtx, ModificationType modificationType) {

        StringBuilder header_log = new StringBuilder();
        StringBuilder log = new StringBuilder();
        header_log.append("Operation [" + modificationType
                + "] validate data of leafref TARGET node: name["
                + referencedByCtx.getCurrentNodeQName() + "] = value["
                + leaf.getValue() + "]");

        if (validatedLeafRefCtx.contains(referencedByCtx)) {
            header_log.append(" -> SKIP: Already validated");
            LOG.debug(header_log.toString());
            return;
        }

        Map<QName, LeafRefContext> allReferencedByLeafRefCtxs = referencedByCtx
                .getAllReferencedByLeafRefCtxs();

        HashMap<LeafRefContext, HashSet> leafRefsValues = new HashMap<LeafRefContext, HashSet>();
        Collection<LeafRefContext> leafrefs = allReferencedByLeafRefCtxs
                .values();
        for (LeafRefContext leafRefContext : leafrefs) {
            if (leafRefContext.isReferencing()) {
                HashSet values = new HashSet();

                SchemaPath leafRefNodeSchemaPath = leafRefContext
                        .getCurrentNodePath();
                LeafRefPath leafRefNodePath = LeafRefUtils
                        .schemaPathToLeafRefPath(leafRefNodeSchemaPath,
                                leafRefContext.getLeafRefContextModule());
                Iterable<QNameWithPredicate> pathFromRoot = leafRefNodePath
                        .getPathFromRoot();
                addValues(values, tree.getRootNode().getDataAfter(),
                        pathFromRoot, null, QNameWithPredicate.ROOT);
                leafRefsValues.put(leafRefContext, values);
            }
        }

        HashSet leafRefTargetNodeValues = new HashSet();
        SchemaPath nodeSchemaPath = referencedByCtx.getCurrentNodePath();
        LeafRefPath nodePath = LeafRefUtils.schemaPathToLeafRefPath(
                nodeSchemaPath, referencedByCtx.getLeafRefContextModule());
        addValues(leafRefTargetNodeValues, tree.getRootNode().getDataAfter(),
                nodePath.getPathFromRoot(), null, QNameWithPredicate.ROOT);

        boolean valid = true;
        Set<Entry<LeafRefContext, HashSet>> entrySet = leafRefsValues
                .entrySet();
        for (Entry<LeafRefContext, HashSet> entry : entrySet) {
            LeafRefContext leafRefContext = entry.getKey();
            HashSet leafRefValuesSet = entry.getValue();
            for (Object leafRefsValue : leafRefValuesSet) {
                if (!leafRefTargetNodeValues.contains(leafRefsValue)) {

                    StringBuilder sb = createInvalidTargetMessage(leaf,
                            leafRefTargetNodeValues, leafRefContext,
                            leafRefsValue);
                    log.append(NEW_LINE);
                    log.append(sb.toString());
                    log.append(" -> FAILED");

                    sb.append(NEW_LINE);
                    errorsMessages.add(sb.toString());

                    valid = false;
                } else {
                    log.append(NEW_LINE);
                    log.append("Valid leafref value [");
                    log.append(leafRefsValue);
                    log.append("]");
                    log.append(" -> OK");
                }
            }
        }

        header_log.append(valid ? " -> OK" : " -> FAILED");
        LOG.debug(header_log.append(log.toString()).toString());

        validatedLeafRefCtx.add(referencedByCtx);
    }

    private StringBuilder createInvalidTargetMessage(NormalizedNode<?, ?> leaf,
            HashSet leafRefTargetNodeValues, LeafRefContext leafRefContext,
            Object leafRefsValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid leafref value [");
        sb.append(leafRefsValue);
        sb.append("]");
        sb.append(" allowed values ");
        sb.append(leafRefTargetNodeValues);
        sb.append(" by validation of leafref TARGET node: ");
        sb.append(leaf.getNodeType());
        sb.append(" path of invalid LEAFREF node: ");
        sb.append(leafRefContext.getCurrentNodePath());
        sb.append(" leafRef target path: ");
        sb.append(leafRefContext.getAbsoluteLeafRefTargetPath());
        return sb;
    }

    private void validateLeafRefNodeData(NormalizedNode<?, ?> leaf,
            LeafRefContext referencingCtx, ModificationType modificationType,
            YangInstanceIdentifier current) {

        StringBuilder header_log = new StringBuilder();
        StringBuilder log = new StringBuilder();

        header_log.append("Operation [" + modificationType
                + "] validate data of LEAFREF node: name["
                + referencingCtx.getCurrentNodeQName() + "] = value["
                + leaf.getValue() + "]");

        HashSet values = new HashSet();
        LeafRefPath targetPath = referencingCtx.getAbsoluteLeafRefTargetPath();
        Iterable<QNameWithPredicate> pathFromRoot = targetPath
                .getPathFromRoot();

        addValues(values, tree.getRootNode().getDataAfter(), pathFromRoot,
                current, QNameWithPredicate.ROOT);

        if (!values.contains(leaf.getValue())) {
            StringBuilder sb = createInvalidLeafRefMessage(leaf,
                    referencingCtx, values);
            errorsMessages.add(sb.toString());

            header_log.append(" -> FAILED");
            log.append(sb.toString());
        } else {
            header_log.append(" -> OK");
        }

        LOG.debug(header_log.toString());
        if (!log.toString().equals(""))
            LOG.debug(log.toString());
    }

    private StringBuilder createInvalidLeafRefMessage(NormalizedNode<?, ?> leaf,
            LeafRefContext referencingCtx, HashSet values) {
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid leafref value [");
        sb.append(leaf.getValue());
        sb.append("]");
        sb.append(" allowed values ");
        sb.append(values);
        sb.append(" of LEAFREF node: ");
        sb.append(leaf.getNodeType());
        sb.append(" leafRef target path: ");
        sb.append(referencingCtx.getAbsoluteLeafRefTargetPath());
        sb.append(NEW_LINE);
        return sb;
    }

    private void addValues(HashSet values,
            Optional<? extends NormalizedNode<?, ?>> optDataNode,
            Iterable<QNameWithPredicate> path, YangInstanceIdentifier current,
            QNameWithPredicate previousQName) {

        if (!optDataNode.isPresent()) {
            return;
        }
        NormalizedNode<?, ?> node = optDataNode.get();

        if (node instanceof LeafNode || node instanceof LeafSetEntryNode) {
            values.add(node.getValue());
            return;
        } else if (node instanceof LeafSetNode<?>) {
            LeafSetNode<?> leafSetNode = (LeafSetNode<?>) node;
            Iterable<? extends NormalizedNode<?, ?>> entries = leafSetNode
                    .getValue();
            for (NormalizedNode<?, ?> entry : entries) {
                values.add(entry.getValue());
            }
            return;
        }

        Iterator<QNameWithPredicate> iterator = path.iterator();
        if (!iterator.hasNext()) {
            return;
        }
        QNameWithPredicate qnameWithPredicate = iterator.next();
        QName qName = qnameWithPredicate.getQName();
        PathArgument pathArgument = toPathArgument(qName);

        if (node instanceof DataContainerNode) {
            DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            Optional<DataContainerChild<? extends PathArgument, ?>> child = dataContainerNode
                    .getChild(pathArgument);

            if (child.isPresent()) {
                addValues(values, child, nextLevel(path), current,
                        qnameWithPredicate);
            } else {
                Iterable<ChoiceNode> choiceNodes = getChoiceNodes(dataContainerNode);
                for (ChoiceNode choiceNode : choiceNodes) {
                    addValues(values, Optional.of(choiceNode), path, current,
                            qnameWithPredicate);
                }
            }

        } else if (node instanceof MapNode) {
            MapNode map = (MapNode) node;
            LinkedList<QNamePredicate> qNamePredicates = previousQName
                    .getQNamePredicates();
            if (qNamePredicates.isEmpty() || current == null) {
                Iterable<MapEntryNode> value = map.getValue();
                for (MapEntryNode mapEntryNode : value) {
                    Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                            .getChild(pathArgument);

                    if (child.isPresent()) {
                        addValues(values, child, nextLevel(path), current,
                                qnameWithPredicate);
                    } else {
                        Iterable<ChoiceNode> choiceNodes = getChoiceNodes(mapEntryNode);
                        for (ChoiceNode choiceNode : choiceNodes) {
                            addValues(values, Optional.of(choiceNode), path,
                                    current, qnameWithPredicate);
                        }
                    }
                }
            } else {
                Map<QName, HashSet> keyValues = new HashMap<QName, HashSet>();

                Iterator<QNamePredicate> predicates = qNamePredicates
                        .iterator();
                while (predicates.hasNext()) {
                    QNamePredicate predicate = predicates.next();
                    QName identifier = predicate.getIdentifier();
                    LeafRefPath predicatePathKeyExpression = predicate
                            .getPathKeyExpression();

                    HashSet pathKeyExprValues = getPathKeyExpressionValues(
                            predicatePathKeyExpression, current);

                    keyValues.put(identifier, pathKeyExprValues);
                }

                Iterable<MapEntryNode> mapEntryNodes = map.getValue();
                for (MapEntryNode mapEntryNode : mapEntryNodes) {
                    if (isMatchingPredicate(mapEntryNode, keyValues)) {
                        Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                                .getChild(pathArgument);

                        if (child.isPresent()) {
                            addValues(values, child, nextLevel(path), current,
                                    qnameWithPredicate);
                        } else {
                            Iterable<ChoiceNode> choiceNodes = getChoiceNodes(mapEntryNode);
                            for (ChoiceNode choiceNode : choiceNodes) {
                                addValues(values, Optional.of(choiceNode),
                                        path, current, qnameWithPredicate);
                            }
                        }
                    }
                }

            }
        }
    }

    private Iterable<ChoiceNode> getChoiceNodes(
            DataContainerNode<?> dataContainerNode) {

        LinkedList<ChoiceNode> choiceNodes = new LinkedList<ChoiceNode>();

        Iterable<DataContainerChild<? extends PathArgument, ?>> childs = dataContainerNode
                .getValue();
        for (DataContainerChild<? extends PathArgument, ?> child : childs) {
            if (child instanceof ChoiceNode) {
                choiceNodes.add((ChoiceNode) child);
            }
        }
        return choiceNodes;
    }

    private boolean isMatchingPredicate(MapEntryNode mapEntryNode,
            Map<QName, HashSet> allowedKeyValues) {

        NodeIdentifierWithPredicates identifier = mapEntryNode.getIdentifier();
        Map<QName, Object> entryKeyValues = identifier.getKeyValues();

        Set<Entry<QName, Object>> entryKeyValueSet = entryKeyValues.entrySet();
        for (Entry<QName, Object> entryKeyValue : entryKeyValueSet) {
            QName key = entryKeyValue.getKey();
            Object value = entryKeyValue.getValue();

            HashSet allowedValues = allowedKeyValues.get(key);
            if (allowedValues != null && !allowedValues.contains(value)) {
                return false;
            }

        }

        return true;
    }

    private HashSet getPathKeyExpressionValues(
            LeafRefPath predicatePathKeyExpression,
            YangInstanceIdentifier current) {

        Optional<NormalizedNode<?, ?>> parent = findParentNode(tree
                .getRootNode().getDataAfter(), current);

        Iterable<QNameWithPredicate> predicatePathExpr = predicatePathKeyExpression
                .getPathFromRoot();
        Iterable<QNameWithPredicate> predicatePath = nextLevel(predicatePathExpr);

        HashSet values = new HashSet();
        if (parent != null) {
            addValues(values, parent, predicatePath, null,
                    QNameWithPredicate.ROOT);
        }

        return values;
    }

    private Optional<NormalizedNode<?, ?>> findParentNode(
            Optional<NormalizedNode<?, ?>> root, YangInstanceIdentifier path) {
        Optional<NormalizedNode<?, ?>> currentNode = root;
        Iterator<PathArgument> pathIterator = path.getPathArguments()
                .iterator();
        while (pathIterator.hasNext()) {
            PathArgument childPathArgument = pathIterator.next();
            if (pathIterator.hasNext() && currentNode.isPresent()) {
                currentNode = NormalizedNodes.getDirectChild(currentNode.get(),
                        childPathArgument);
            } else {
                return currentNode;
            }
        }
        return Optional.absent();
    }

    private Iterable<QNameWithPredicate> nextLevel(
            final Iterable<QNameWithPredicate> path) {
        return Iterables.skip(path, 1);
    }

    private PathArgument toPathArgument(QName qName) {
        return YangInstanceIdentifier.of(qName).getLastPathArgument();
    }
}
