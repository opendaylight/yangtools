/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LeafRefValidation {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidation.class);
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final Set<LeafRefContext> validatedLeafRefCtx = new HashSet<>();
    private final List<String> errorsMessages = new ArrayList<>();
    private final NormalizedNode root;

    private LeafRefValidation(final NormalizedNode root) {
        this.root = root;
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        final var root = tree.getRootNode().dataAfter();
        if (root != null) {
            new LeafRefValidation(root).validateChildren(rootLeafRefCtx, tree.getRootNode().childNodes());
        }
    }

    private void validateChildren(final LeafRefContext rootLeafRefCtx, final Collection<DataTreeCandidateNode> children)
            throws LeafRefDataValidationFailedException {
        for (var dataTreeCandidateNode : children) {
            if (dataTreeCandidateNode.modificationType() != ModificationType.UNMODIFIED) {
                final PathArgument identifier = dataTreeCandidateNode.name();
                final QName childQName = identifier.getNodeType();

                final LeafRefContext referencedByCtx = rootLeafRefCtx.getReferencedChildByName(childQName);
                final LeafRefContext referencingCtx = rootLeafRefCtx.getReferencingChildByName(childQName);
                if (referencedByCtx != null || referencingCtx != null) {
                    validateNode(dataTreeCandidateNode, referencedByCtx, referencingCtx,
                        YangInstanceIdentifier.of(identifier));
                }
            }
        }

        if (!errorsMessages.isEmpty()) {
            final StringBuilder message = new StringBuilder();
            int errCount = 0;
            for (var errorMessage : errorsMessages) {
                message.append(errorMessage);
                errCount++;
            }
            throw new LeafRefDataValidationFailedException(message.toString(), errCount);
        }
    }

    private void validateNode(final DataTreeCandidateNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final YangInstanceIdentifier current) {
        final var modType = node.modificationType();

        if (modType == ModificationType.WRITE) {
            final var dataAfter = node.dataAfter();
            if (dataAfter != null) {
                validateNodeData(dataAfter, referencedByCtx, referencingCtx, ModificationType.WRITE, current);
            }
            return;
        }

        if (modType == ModificationType.DELETE && referencedByCtx != null) {
            validateNodeData(verifyNotNull(node.dataBefore()), referencedByCtx, null, node.modificationType(), current);
            return;
        }

        for (var childNode : node.childNodes()) {
            if (childNode.modificationType() != ModificationType.UNMODIFIED) {
                final LeafRefContext childReferencedByCtx = getReferencedByCtxChild(referencedByCtx, childNode);
                final LeafRefContext childReferencingCtx = getReferencingCtxChild(referencingCtx, childNode);

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    validateNode(childNode, childReferencedByCtx,childReferencingCtx, current.node(childNode.name()));
                }
            }
        }
    }

    private static LeafRefContext getReferencingCtxChild(final LeafRefContext referencingCtx,
            final DataTreeCandidateNode childNode) {
        if (referencingCtx == null) {
            return null;
        }

        final QName childQName = childNode.name().getNodeType();
        LeafRefContext childReferencingCtx = referencingCtx.getReferencingChildByName(childQName);
        if (childReferencingCtx == null) {
            final NormalizedNode data = verifyNotNull(childNode.dataAfter());
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

        final QName childQName = childNode.name().getNodeType();
        LeafRefContext childReferencedByCtx = referencedByCtx.getReferencedChildByName(childQName);
        if (childReferencedByCtx == null) {
            final NormalizedNode data = verifyNotNull(childNode.dataAfter());
            if (data instanceof MapEntryNode || data instanceof UnkeyedListEntryNode) {
                childReferencedByCtx = referencedByCtx;
            }
        }

        return childReferencedByCtx;
    }

    private void validateNodeData(final NormalizedNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        switch (node) {
            case LeafNode<?> leaf ->
                validateLeafNodeData(leaf, referencedByCtx, referencingCtx, modificationType, current);
            case LeafSetNode<?> leafSet ->
                validateLeafSetNodeData(leafSet, referencedByCtx, referencingCtx, modificationType, current);
            case ChoiceNode choice ->
                validateChoiceNodeData(choice, referencedByCtx, referencingCtx, modificationType, current);
            case DataContainerNode container ->
                validateDataContainerNodeData(container, referencedByCtx, referencingCtx, modificationType, current);
            case MapNode map ->
                validateMapNodeData(map, referencedByCtx, referencingCtx, modificationType, current);
            default -> {
                // FIXME: check UnkeyedListNode case
            }
        }
    }

    private void validateLeafNodeData(final LeafNode<?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        if (referencedByCtx != null && referencedByCtx.isReferenced()) {
            validateLeafRefTargetNodeData(node, referencedByCtx, modificationType);
        }
        if (referencingCtx != null && referencingCtx.isReferencing()) {
            validateLeafRefNodeData(node, referencingCtx, modificationType, current);
        }
    }

    private void validateLeafSetNodeData(final LeafSetNode<?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        if (referencedByCtx != null || referencingCtx != null) {
            for (var leafSetEntry : node.body()) {
                if (referencedByCtx != null && referencedByCtx.isReferenced()) {
                    validateLeafRefTargetNodeData(leafSetEntry, referencedByCtx, modificationType);
                }
                if (referencingCtx != null && referencingCtx.isReferencing()) {
                    validateLeafRefNodeData(leafSetEntry, referencingCtx, modificationType, current);
                }
            }
        }
    }

    private void validateChoiceNodeData(final ChoiceNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (var child : node.body()) {
            final QName qname = child.name().getNodeType();
            final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                    : findReferencedByCtxUnderChoice(referencedByCtx, qname);
            final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                    : findReferencingCtxUnderChoice(referencingCtx, qname);
            if (childReferencedByCtx != null || childReferencingCtx != null) {
                validateNodeData(child, childReferencedByCtx, childReferencingCtx, modificationType,
                    current.node(child.name()));
            }
        }
    }

    private void validateDataContainerNodeData(final DataContainerNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (var child : node.body()) {
            validateChildNodeData(child, referencedByCtx, referencingCtx, modificationType, current);
        }
    }

    private void validateMapNodeData(final MapNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (var mapEntry : node.asMap().values()) {
            final var mapEntryIdentifier = current.node(mapEntry.name());
            for (var child : mapEntry.body()) {
                validateChildNodeData(child, referencedByCtx, referencingCtx, modificationType, mapEntryIdentifier);
            }
        }
    }

    private void validateChildNodeData(final DataContainerChild child, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        final var qname = child.name().getNodeType();
        final var childReferencedByCtx = referencedByCtx == null ? null
                : referencedByCtx.getReferencedChildByName(qname);
        final var childReferencingCtx = referencingCtx == null ? null : referencingCtx.getReferencingChildByName(qname);
        if (childReferencedByCtx != null || childReferencingCtx != null) {
            validateNodeData(child, childReferencedByCtx, childReferencingCtx, modificationType,
                current.node(child.name()));
        }
    }

    private static LeafRefContext findReferencingCtxUnderChoice(final LeafRefContext referencingCtx,
            final QName qname) {
        for (var child : referencingCtx.getReferencingChilds().values()) {
            final var referencingChildByName = child.getReferencingChildByName(qname);
            if (referencingChildByName != null) {
                return referencingChildByName;
            }
        }
        return null;
    }

    private static LeafRefContext findReferencedByCtxUnderChoice(final LeafRefContext referencedByCtx,
            final QName qname) {
        for (var child : referencedByCtx.getReferencedByChilds().values()) {
            final var referencedByChildByName = child.getReferencedChildByName(qname);
            if (referencedByChildByName != null) {
                return referencedByChildByName;
            }
        }
        return null;
    }

    private void validateLeafRefTargetNodeData(final NormalizedNode leaf, final LeafRefContext
            referencedByCtx, final ModificationType modificationType) {
        if (!validatedLeafRefCtx.add(referencedByCtx)) {
            LOG.trace(
                "Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}] -> SKIP: Already validated",
                modificationType, referencedByCtx.getNodeName(), leaf.body());
            return;
        }

        LOG.trace("Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}]", modificationType,
            referencedByCtx.getNodeName(), leaf.body());
        final var leafRefs = referencedByCtx.getAllReferencedByLeafRefCtxs().values().stream()
                .filter(LeafRefContext::isReferencing)
                .collect(Collectors.toSet());
        if (leafRefs.isEmpty()) {
            return;
        }

        final var leafRefTargetNodeValues = extractRootValues(referencedByCtx);
        leafRefs.forEach(leafRefContext -> {
            extractRootValues(leafRefContext).forEach(leafRefsValue -> {
                if (leafRefTargetNodeValues.contains(leafRefsValue)) {
                    LOG.trace("Valid leafref value [{}] {}", leafRefsValue, SUCCESS);
                    return;
                }

                LOG.debug("Invalid leafref value [{}] allowed values {} by validation of leafref TARGET node: {} path "
                        + "of invalid LEAFREF node: {} leafRef target path: {} {}", leafRefsValue,
                        leafRefTargetNodeValues, leaf.name(), leafRefContext.getCurrentNodePath(),
                        leafRefContext.getAbsoluteLeafRefTargetPath(), FAILED);
                errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s by validation of leafref"
                        + " TARGET node: %s path of invalid LEAFREF node: %s leafRef target path: %s %s", leafRefsValue,
                        leafRefTargetNodeValues, leaf.name(), leafRefContext.getCurrentNodePath(),
                        leafRefContext.getAbsoluteLeafRefTargetPath(),
                        FAILED));
            });
        });
    }

    private Set<Object> extractRootValues(final LeafRefContext context) {
        return computeValues(root, createPath(context.getLeafRefNodePath()), null);
    }

    private void validateLeafRefNodeData(final NormalizedNode leaf, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {
        final var values = computeValues(root, createPath(referencingCtx.getAbsoluteLeafRefTargetPath()), current);
        if (values.contains(leaf.body())) {
            LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}", modificationType,
                referencingCtx.getNodeName(), leaf.body(), SUCCESS);
            return;
        }

        LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}", modificationType,
            referencingCtx.getNodeName(), leaf.body(), FAILED);
        LOG.debug("Invalid leafref value [{}] allowed values {} of LEAFREF node: {} leafRef target path: {}",
            leaf.body(), values, leaf.name(), referencingCtx.getAbsoluteLeafRefTargetPath());
        errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s of LEAFREF node: %s leafRef "
                + "target path: %s", leaf.body(), values, leaf.name(),
                referencingCtx.getAbsoluteLeafRefTargetPath()));
    }

    private Set<Object> computeValues(final NormalizedNode node, final ArrayDeque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        final var values = new HashSet<>();
        addValues(values, node, ImmutableList.of(), path, current);
        return values;
    }

    private void addValues(final Set<Object> values, final NormalizedNode node,
            final List<QNamePredicate> nodePredicates, final ArrayDeque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        if (node instanceof ValueNode) {
            values.add(node.body());
            return;
        }
        if (node instanceof LeafSetNode<?> leafSet) {
            for (var entry : leafSet.body()) {
                values.add(entry.body());
            }
            return;
        }

        final QNameWithPredicate next = path.peek();
        if (next == null) {
            return;
        }

        final var pathArgument = new NodeIdentifier(next.getQName());
        if (node instanceof DataContainerNode dataContainer) {
            processChildNode(values, dataContainer, pathArgument, next.getQNamePredicates(), path, current);
        } else if (node instanceof MapNode map) {
            var entries = map.body().stream();
            if (!nodePredicates.isEmpty() && current != null) {
                entries = entries.filter(createMapEntryPredicate(nodePredicates, current));
            }

            entries.forEach(entry -> processChildNode(values, entry, pathArgument, next.getQNamePredicates(), path,
                current));
        }
    }

    private void processChildNode(final Set<Object> values, final DataContainerNode parent,
            final NodeIdentifier arg, final List<QNamePredicate> nodePredicates,
            final ArrayDeque<QNameWithPredicate> path, final YangInstanceIdentifier current) {
        final var child = parent.childByArg(arg);
        if (child == null) {
            // FIXME: YANGTOOLS-901. We have SchemaContext nearby, hence we should be able to cache how to get
            //        to the leaf with with specified QName, without having to iterate through Choices.
            //        That perhaps means we should not have QNameWithPredicates, but NodeIdentifierWithPredicates as
            //        the path specification.
            for (var mixin : parent.body()) {
                if (mixin instanceof ChoiceNode) {
                    addValues(values, mixin, nodePredicates, path, current);
                }
            }
        } else {
            addNextValues(values, child, nodePredicates, path, current);
        }
    }

    private Predicate<MapEntryNode> createMapEntryPredicate(final List<QNamePredicate> nodePredicates,
            final YangInstanceIdentifier current) {
        final var keyValues = new HashMap<QName, Set<?>>();
        for (var predicate : nodePredicates) {
            keyValues.put(predicate.getIdentifier(), getPathKeyExpressionValues(predicate.getPathKeyExpression(),
                current));
        }

        return mapEntry -> {
            for (var entryKeyValue : mapEntry.name().entrySet()) {
                final var allowedValues = keyValues.get(entryKeyValue.getKey());
                if (allowedValues != null && !allowedValues.contains(entryKeyValue.getValue())) {
                    return false;
                }
            }
            return true;
        };
    }

    private void addNextValues(final Set<Object> values, final DataContainerChild node,
            final List<QNamePredicate> nodePredicates, final ArrayDeque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        final var element = path.pop();
        try {
            addValues(values, node, nodePredicates, path, current);
        } finally {
            path.push(element);
        }
    }

    private Set<?> getPathKeyExpressionValues(final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {
        return findParentNode(Optional.of(root), current).map(parent -> {
            final var path = createPath(predicatePathKeyExpression);
            path.pollFirst();
            return computeValues(parent, path, null);
        }).orElse(ImmutableSet.of());
    }

    private static Optional<NormalizedNode> findParentNode(
            final Optional<NormalizedNode> root, final YangInstanceIdentifier path) {
        var currentNode = root;
        final var pathIterator = path.getPathArguments().iterator();
        while (pathIterator.hasNext()) {
            final var childPathArgument = pathIterator.next();
            if (pathIterator.hasNext() && currentNode.isPresent()) {
                currentNode = NormalizedNodes.getDirectChild(currentNode.orElseThrow(), childPathArgument);
            } else {
                return currentNode;
            }
        }
        return Optional.empty();
    }

    private static ArrayDeque<QNameWithPredicate> createPath(final LeafRefPath path) {
        final var ret = new ArrayDeque<QNameWithPredicate>();
        path.getPathTowardsRoot().forEach(ret::push);
        return ret;
    }
}
