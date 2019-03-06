/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LeafRefValidation {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidation.class);
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final Set<LeafRefContext> validatedLeafRefCtx = new HashSet<>();
    private final List<String> errorsMessages = new ArrayList<>();
    private final NormalizedNode<?, ?> root;

    private LeafRefValidation(final NormalizedNode<?, ?> root) {
        this.root = root;
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        final Optional<NormalizedNode<?, ?>> root = tree.getRootNode().getDataAfter();
        if (root.isPresent()) {
            new LeafRefValidation(root.get()).validateChildren(rootLeafRefCtx, tree.getRootNode().getChildNodes());
        }
    }

    private void validateChildren(final LeafRefContext rootLeafRefCtx, final Collection<DataTreeCandidateNode> children)
            throws LeafRefDataValidationFailedException {
        for (final DataTreeCandidateNode dataTreeCandidateNode : children) {
            if (dataTreeCandidateNode.getModificationType() != ModificationType.UNMODIFIED) {
                final PathArgument identifier = dataTreeCandidateNode.getIdentifier();
                final QName childQName = identifier.getNodeType();

                final LeafRefContext referencedByCtx = rootLeafRefCtx.getReferencedChildByName(childQName);
                final LeafRefContext referencingCtx = rootLeafRefCtx.getReferencingChildByName(childQName);
                if (referencedByCtx != null || referencingCtx != null) {
                    validateNode(dataTreeCandidateNode, referencedByCtx, referencingCtx,
                        YangInstanceIdentifier.create(identifier));
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
            validateNodeData(node.getDataAfter().get(), referencedByCtx, referencingCtx, node.getModificationType(),
                current);
            return;
        }

        if (node.getModificationType() == ModificationType.DELETE && referencedByCtx != null) {
            validateNodeData(node.getDataBefore().get(), referencedByCtx, null, node.getModificationType(), current);
            return;
        }

        for (final DataTreeCandidateNode childNode : node.getChildNodes()) {
            if (childNode.getModificationType() != ModificationType.UNMODIFIED) {
                final LeafRefContext childReferencedByCtx = getReferencedByCtxChild(referencedByCtx, childNode);
                final LeafRefContext childReferencingCtx = getReferencingCtxChild(referencingCtx, childNode);

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    validateNode(childNode, childReferencedByCtx,childReferencingCtx,
                        current.node(childNode.getIdentifier()));
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

    private void validateNodeData(final NormalizedNode<?, ?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        if (node instanceof LeafNode) {
            validateLeafNodeData((LeafNode<?>) node, referencedByCtx, referencingCtx, modificationType, current);
        } else if (node instanceof LeafSetNode) {
            validateLeafSetNodeData((LeafSetNode<?>) node, referencedByCtx, referencingCtx, modificationType, current);
        } else if (node instanceof ChoiceNode) {
            validateChoiceNodeData((ChoiceNode) node, referencedByCtx, referencingCtx, modificationType, current);
        } else if (node instanceof DataContainerNode) {
            validateDataContainerNodeData((DataContainerNode<?>) node, referencedByCtx, referencingCtx,
                modificationType, current);
        } else if (node instanceof MapNode) {
            validateMapNodeData((MapNode) node, referencedByCtx, referencingCtx, modificationType, current);
        }
        // FIXME: check UnkeyedListNode case
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
            for (final NormalizedNode<?, ?> leafSetEntry : node.getValue()) {
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
        for (final DataContainerChild<?, ?> child : node.getValue()) {
            final QName qname = child.getNodeType();
            final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                    : findReferencedByCtxUnderChoice(referencedByCtx, qname);
            final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                    : findReferencingCtxUnderChoice(referencingCtx, qname);
            if (childReferencedByCtx != null || childReferencingCtx != null) {
                validateNodeData(child, childReferencedByCtx, childReferencingCtx, modificationType,
                    current.node(child.getIdentifier()));
            }
        }
    }

    private void validateDataContainerNodeData(final DataContainerNode<?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (final DataContainerChild<?, ?> child : node.getValue()) {
            if (child instanceof AugmentationNode) {
                validateNodeData(child, referencedByCtx, referencingCtx, modificationType, current.node(
                    child.getIdentifier()));
                return;
            }

            validateChildNodeData(child, referencedByCtx, referencingCtx, modificationType, current);
        }
    }

    private void validateMapNodeData(final MapNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (final MapEntryNode mapEntry : node.getValue()) {
            final YangInstanceIdentifier mapEntryIdentifier = current.node(mapEntry.getIdentifier());
            for (final DataContainerChild<?, ?> child : mapEntry.getValue()) {
                if (child instanceof AugmentationNode) {
                    validateNodeData(child, referencedByCtx, referencingCtx, modificationType, current.node(
                        child.getIdentifier()));
                    return;
                }

                validateChildNodeData(child, referencedByCtx, referencingCtx, modificationType, mapEntryIdentifier);
            }
        }
    }

    private void validateChildNodeData(final DataContainerChild<?, ?> child, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        final QName qname = child.getNodeType();
        final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                : referencedByCtx.getReferencedChildByName(qname);
        final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                : referencingCtx.getReferencingChildByName(qname);
        if (childReferencedByCtx != null || childReferencingCtx != null) {
            validateNodeData(child, childReferencedByCtx, childReferencingCtx, modificationType, current.node(
                child.getIdentifier()));
        }
    }

    private static LeafRefContext findReferencingCtxUnderChoice(final LeafRefContext referencingCtx,
            final QName qname) {
        for (final LeafRefContext child : referencingCtx.getReferencingChilds().values()) {
            final LeafRefContext referencingChildByName = child.getReferencingChildByName(qname);
            if (referencingChildByName != null) {
                return referencingChildByName;
            }
        }
        return null;
    }

    private static LeafRefContext findReferencedByCtxUnderChoice(final LeafRefContext referencedByCtx,
            final QName qname) {
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
        if (!validatedLeafRefCtx.add(referencedByCtx)) {
            LOG.trace("Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}] -> SKIP: Already "
                    + "validated", modificationType, referencedByCtx.getNodeName(), leaf.getValue());
            return;
        }

        LOG.trace("Operation [{}] validate data of leafref TARGET node: name[{}] = value[{}]", modificationType,
            referencedByCtx.getNodeName(), leaf.getValue());
        final Set<LeafRefContext> leafRefs = referencedByCtx.getAllReferencedByLeafRefCtxs().values().stream()
                .filter(LeafRefContext::isReferencing).collect(Collectors.toSet());
        if (leafRefs.isEmpty()) {
            return;
        }

        final Set<Object> leafRefTargetNodeValues = extractRootValues(referencedByCtx);
        leafRefs.forEach(leafRefContext -> {
            extractRootValues(leafRefContext).forEach(leafRefsValue -> {
                if (leafRefTargetNodeValues.contains(leafRefsValue)) {
                    LOG.trace("Valid leafref value [{}] {}", leafRefsValue, SUCCESS);
                    return;
                }

                LOG.debug("Invalid leafref value [{}] allowed values {} by validation of leafref TARGET node: {} path "
                        + "of invalid LEAFREF node: {} leafRef target path: {} {}", leafRefsValue,
                        leafRefTargetNodeValues, leaf.getNodeType(), leafRefContext.getCurrentNodePath(),
                        leafRefContext.getAbsoluteLeafRefTargetPath(), FAILED);
                errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s by validation of leafref"
                        + " TARGET node: %s path of invalid LEAFREF node: %s leafRef target path: %s %s", leafRefsValue,
                        leafRefTargetNodeValues, leaf.getNodeType(), leafRefContext.getCurrentNodePath(),
                        leafRefContext.getAbsoluteLeafRefTargetPath(),
                        FAILED));
            });
        });
    }

    private Set<Object> extractRootValues(final LeafRefContext context) {
        return computeValues(root, createPath(context.getLeafRefNodePath()), null);
    }

    private void validateLeafRefNodeData(final NormalizedNode<?, ?> leaf, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {
        final Set<Object> values = computeValues(root, createPath(referencingCtx.getAbsoluteLeafRefTargetPath()),
            current);
        if (values.contains(leaf.getValue())) {
            LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}", modificationType,
                referencingCtx.getNodeName(), leaf.getValue(), SUCCESS);
            return;
        }

        LOG.debug("Operation [{}] validate data of LEAFREF node: name[{}] = value[{}] {}", modificationType,
            referencingCtx.getNodeName(), leaf.getValue(), FAILED);
        LOG.debug("Invalid leafref value [{}] allowed values {} of LEAFREF node: {} leafRef target path: {}",
            leaf.getValue(), values, leaf.getNodeType(), referencingCtx.getAbsoluteLeafRefTargetPath());
        errorsMessages.add(String.format("Invalid leafref value [%s] allowed values %s of LEAFREF node: %s leafRef "
                + "target path: %s", leaf.getValue(), values, leaf.getNodeType(),
                referencingCtx.getAbsoluteLeafRefTargetPath()));
    }

    private Set<Object> computeValues(final NormalizedNode<?, ?> node, final Deque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        final HashSet<Object> values = new HashSet<>();
        addValues(values, node, ImmutableList.of(), path, current);
        return values;
    }

    private void addValues(final Set<Object> values, final NormalizedNode<?, ?> node,
            final List<QNamePredicate> nodePredicates, final Deque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
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

        final QNameWithPredicate next = path.peek();
        if (next == null) {
            return;
        }

        final PathArgument pathArgument = new NodeIdentifier(next.getQName());
        if (node instanceof DataContainerNode) {
            processChildNode(values, (DataContainerNode<?>) node, pathArgument, next.getQNamePredicates(), path,
                current);
        } else if (node instanceof MapNode) {
            Stream<MapEntryNode> entries = ((MapNode) node).getValue().stream();
            if (!nodePredicates.isEmpty() && current != null) {
                entries = entries.filter(createMapEntryPredicate(nodePredicates, current));
            }

            entries.forEach(entry -> processChildNode(values, entry, pathArgument, next.getQNamePredicates(), path,
                current));
        }
    }

    private void processChildNode(final Set<Object> values, final DataContainerNode<?> parent,
            final PathArgument arg, final List<QNamePredicate> nodePredicates, final Deque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        final Optional<DataContainerChild<?, ?>> child = parent.getChild(arg);
        if (!child.isPresent()) {
            // FIXME: YANGTOOLS-901. We have SchemaContext nearby, hence we should be able to cache how to get
            //        to the leaf with with specified QName, without having to iterate through Choices/Augmentations.
            //        That perhaps means we should not have QNameWithPredicates, but NodeIdentifierWithPredicates as
            //        the path specification.
            for (final DataContainerChild<?, ?> mixin : parent.getValue()) {
                if (mixin instanceof AugmentationNode || mixin instanceof ChoiceNode) {
                    addValues(values, mixin, nodePredicates, path, current);
                }
            }
        } else {
            addNextValues(values, child.get(), nodePredicates, path, current);
        }
    }

    private Predicate<MapEntryNode> createMapEntryPredicate(final List<QNamePredicate> nodePredicates,
            final YangInstanceIdentifier current) {
        final Map<QName, Set<?>> keyValues = new HashMap<>();
        for (QNamePredicate predicate : nodePredicates) {
            keyValues.put(predicate.getIdentifier(), getPathKeyExpressionValues(predicate.getPathKeyExpression(),
                current));
        }

        return mapEntry -> {
            for (final Entry<QName, Object> entryKeyValue : mapEntry.getIdentifier().getKeyValues().entrySet()) {
                final Set<?> allowedValues = keyValues.get(entryKeyValue.getKey());
                if (allowedValues != null && !allowedValues.contains(entryKeyValue.getValue())) {
                    return false;
                }
            }
            return true;
        };
    }

    private void addNextValues(final Set<Object> values, final NormalizedNode<?, ?> node,
            final List<QNamePredicate> nodePredicates, final Deque<QNameWithPredicate> path,
            final YangInstanceIdentifier current) {
        final QNameWithPredicate element = path.pop();
        try {
            addValues(values, node, nodePredicates, path, current);
        } finally {
            path.push(element);
        }
    }

    private Set<?> getPathKeyExpressionValues(final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {
        return findParentNode(Optional.of(root), current).map(parent -> {
            final Deque<QNameWithPredicate> path = createPath(predicatePathKeyExpression);
            path.pollFirst();
            return computeValues(parent, path, null);
        }).orElse(ImmutableSet.of());
    }

    private static Optional<NormalizedNode<?, ?>> findParentNode(
            final Optional<NormalizedNode<?, ?>> root, final YangInstanceIdentifier path) {
        Optional<NormalizedNode<?, ?>> currentNode = root;
        final Iterator<PathArgument> pathIterator = path.getPathFromRoot().iterator();
        while (pathIterator.hasNext()) {
            final PathArgument childPathArgument = pathIterator.next();
            if (pathIterator.hasNext() && currentNode.isPresent()) {
                currentNode = NormalizedNodes.getDirectChild(currentNode.get(), childPathArgument);
            } else {
                return currentNode;
            }
        }
        return Optional.empty();
    }

    private static Deque<QNameWithPredicate> createPath(final LeafRefPath path) {
        final Deque<QNameWithPredicate> ret = new ArrayDeque<>();
        path.getPathTowardsRoot().forEach(ret::push);
        return ret;
    }
}
