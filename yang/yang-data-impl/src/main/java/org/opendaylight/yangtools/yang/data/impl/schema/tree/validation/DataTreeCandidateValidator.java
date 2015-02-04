/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree.validation;

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
                    validateNode(dataTreeCandidateNode, referencedByCtx,
                            referencingCtx);
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
            throw new LeafRefDataValidationFailedException(message.toString(),errCount);
        }

    }

    private void validateNode(DataTreeCandidateNode node,
            LeafRefContext referencedByCtx, LeafRefContext referencingCtx) {

        if ((node.getModificationType() == ModificationType.WRITE || node
                .getModificationType() == ModificationType.MERGE)
                && node.getDataAfter().isPresent()) {

            Optional<NormalizedNode<?, ?>> dataAfter = node.getDataAfter();
            NormalizedNode<?, ?> normalizedNode = dataAfter.get();
            validateNodeData(normalizedNode, referencedByCtx, referencingCtx,
                    node.getModificationType());
        } else if (node.getModificationType() == ModificationType.DELETE
                && referencedByCtx != null) {

            Optional<NormalizedNode<?, ?>> dataBefor = node.getDataBefore();
            NormalizedNode<?, ?> normalizedNode = dataBefor.get();
            validateNodeData(normalizedNode, referencedByCtx, null,
                    node.getModificationType());
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
                    validateNode(dataTreeCandidateNode, childReferencedByCtx,
                            childReferencingCtx);
                }
            }

        }

    }

    private void validateNodeData(NormalizedNode<?, ?> node,
            LeafRefContext referencedByCtx, LeafRefContext referencingCtx,
            ModificationType modificationType) {

        if (node instanceof LeafNode) {
            LeafNode<?> leaf = (LeafNode<?>) node;

            if (referencedByCtx != null && referencedByCtx.isReferencedBy()) {
                validateLeafRefTargetNodeData(leaf, referencedByCtx,
                        modificationType);
            }

            if (referencingCtx != null && referencingCtx.isReferencing()) {
                validateLeafRefNodeData(leaf, referencingCtx, modificationType);
            }

            return;
        }

        if (node instanceof LeafSetNode) {
            LeafSetNode<?> leafList = (LeafSetNode<?>) node;

            // :TODO

            return;
        }

        if (node instanceof ChoiceNode) {
            ChoiceNode choice = (ChoiceNode) node;
            Iterable<DataContainerChild<? extends PathArgument, ?>> childs = choice
                    .getValue();
            for (DataContainerChild<? extends PathArgument, ?> dataContainerChild : childs) {
                validateNodeData(dataContainerChild, referencedByCtx,
                        referencingCtx, modificationType);
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
                    validateNodeData(dataContainerChild, childReferencedByCtx,
                            childReferencingCtx, modificationType);
                }
            }
        } else if (node instanceof MapNode) {
            MapNode map = (MapNode) node;
            Iterable<MapEntryNode> mapEntries = map.getValue();
            for (MapEntryNode mapEntry : mapEntries) {
                Iterable<DataContainerChild<? extends PathArgument, ?>> mapEntryNodes = mapEntry
                        .getValue();
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
                        validateNodeData(mapEntryNode, childReferencedByCtx,
                                childReferencingCtx, modificationType);
                    }
                }
            }

        }

        // :TODO if(node instance of UnkeyedListNode ...
    }

    private void validateLeafRefTargetNodeData(LeafNode<?> leaf,
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
                        pathFromRoot);
                leafRefsValues.put(leafRefContext, values);
            }
        }

        HashSet leafRefTargetNodeValues = new HashSet();
        SchemaPath nodeSchemaPath = referencedByCtx.getCurrentNodePath();
        LeafRefPath nodePath = LeafRefUtils.schemaPathToLeafRefPath(
                nodeSchemaPath, referencedByCtx.getLeafRefContextModule());
        addValues(leafRefTargetNodeValues, tree.getRootNode().getDataAfter(),
                nodePath.getPathFromRoot());

        boolean valid = true;
        Set<Entry<LeafRefContext, HashSet>> entrySet = leafRefsValues
                .entrySet();
        for (Entry<LeafRefContext, HashSet> entry : entrySet) {
            LeafRefContext leafRefContext = entry.getKey();
            HashSet leafRefValuesSet = entry.getValue();
            for (Object leafRefsValue : leafRefValuesSet) {
                if (!leafRefTargetNodeValues.contains(leafRefsValue)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Invalid leafref value [");
                    sb.append(leafRefsValue);
                    sb.append("] by validation of leafref TARGET node: ");
                    sb.append(leaf.getNodeType());
                    sb.append(" path of invalid LEAFREF node: ");
                    sb.append(leafRefContext.getCurrentNodePath());
                    sb.append(" leafRef target path: ");
                    sb.append(leafRefContext.getAbsoluteLeafRefTargetPath());
                    sb.append(" possible values ");
                    sb.append(leafRefTargetNodeValues);

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

    private void validateLeafRefNodeData(LeafNode<?> leaf,
            LeafRefContext referencingCtx, ModificationType modificationType) {

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

        addValues(values, tree.getRootNode().getDataAfter(), pathFromRoot);

        if (!values.contains(leaf.getValue())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid leafref value [");
            sb.append(leaf.getValue());
            sb.append("] of LEAFREF node: ");
            sb.append(leaf.getNodeType());
            sb.append(" leafRef target path: ");
            sb.append(referencingCtx.getAbsoluteLeafRefTargetPath());
            sb.append(" possible values ");
            sb.append(values);
            sb.append(NEW_LINE);
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

    private void addValues(HashSet values,
            Optional<? extends NormalizedNode<?, ?>> optDataNode,
            Iterable<QNameWithPredicate> path) {

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
        PathArgument pathArgument = YangInstanceIdentifier.of(qName)
                .getLastPathArgument();

        if (node instanceof DataContainerNode) {
            DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            Optional<DataContainerChild<? extends PathArgument, ?>> child = dataContainerNode
                    .getChild(pathArgument);
            addValues(values, child, nextLevel(path));
        } else if (node instanceof MapNode) {
            MapNode map = (MapNode) node;
            LinkedList<QNamePredicate> qNamePredicates = qnameWithPredicate
                    .getQNamePredicates();
            if (qNamePredicates.isEmpty()) {
                Iterable<MapEntryNode> value = map.getValue();
                for (MapEntryNode mapEntryNode : value) {
                    Optional<DataContainerChild<? extends PathArgument, ?>> child = mapEntryNode
                            .getChild(pathArgument);
                    addValues(values, child, nextLevel(path));
                }
            } else {
                // :TODO check predicates
            }
        }
    }

    private Iterable<QNameWithPredicate> nextLevel(
            final Iterable<QNameWithPredicate> path) {
        return Iterables.skip(path, 1);
    }

}
