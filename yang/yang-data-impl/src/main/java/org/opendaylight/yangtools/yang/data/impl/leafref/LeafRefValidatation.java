/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

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
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: 3.0.0: Rename to LeafRefValidation
public final class LeafRefValidatation {

    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidatation.class);
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final Set<LeafRefContext> validatedLeafRefCtx = new HashSet<>();
    private final List<String> errorsMessages = new ArrayList<>();
    private final DataSchemaContextNode<?> rootContextNode;
    private final NormalizedNode<?, ?> root;

    private LeafRefValidatation(final NormalizedNode<?, ?> root, final DataSchemaContextNode<?> rootContextNode) {
        this.root = requireNonNull(root);
        this.rootContextNode = requireNonNull(rootContextNode);
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        final Optional<NormalizedNode<?, ?>> root = tree.getRootNode().getDataAfter();
        if (root.isPresent()) {
            new LeafRefValidatation(root.get(), DataSchemaContextTree.from(rootLeafRefCtx.getSchemaContext()).getRoot())
            .validateChildren(rootLeafRefCtx, tree.getRootNode().getChildNodes());
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

    private void validateNodeData(final NormalizedNode<?, ?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {

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

            for (final DataContainerChild<? extends PathArgument, ?> child : dataContainerNode.getValue()) {
                if (child instanceof AugmentationNode) {
                    validateNodeData(child, referencedByCtx, referencingCtx, modificationType, current
                        .node(child.getIdentifier()));
                    return;
                }

                final QName qname = child.getNodeType();
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
                            .node(child.getIdentifier());
                    validateNodeData(child, childReferencedByCtx,
                            childReferencingCtx, modificationType, childYangInstanceIdentifier);
                }
            }
        } else if (node instanceof MapNode) {
            final MapNode map = (MapNode) node;

            for (final MapEntryNode mapEntry : map.getValue()) {
                final YangInstanceIdentifier mapEntryYangInstanceIdentifier = current.node(mapEntry.getIdentifier());
                for (final DataContainerChild<? extends PathArgument, ?> mapEntryNode : mapEntry.getValue()) {
                    if (mapEntryNode instanceof AugmentationNode) {
                        validateNodeData(mapEntryNode, referencedByCtx, referencingCtx, modificationType, current
                            .node(mapEntryNode.getIdentifier()));
                        return;
                    }

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
                        validateNodeData(mapEntryNode, childReferencedByCtx, childReferencingCtx, modificationType,
                                mapEntryYangInstanceIdentifier.node(mapEntryNode.getIdentifier()));
                    }
                }
            }
        }
        // FIXME: check UnkeyedListNode case
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
        return computeValues(root, rootContextNode, createPath(context.getLeafRefNodePath()), null);
    }

    private void validateLeafRefNodeData(final NormalizedNode<?, ?> leaf, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {
        final Set<Object> values = computeValues(root, rootContextNode,
            createPath(referencingCtx.getAbsoluteLeafRefTargetPath()), current);
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

    private Set<Object> computeValues(final NormalizedNode<?, ?> node, final DataSchemaContextNode<?> nodeContext,
            final Deque<QNameWithPredicate> iterator, final YangInstanceIdentifier current) {
        final HashSet<Object> values = new HashSet<>();
        addValues(values, node, nodeContext, ImmutableList.of(), iterator, current);
        return values;
    }

    private void addValues(final Set<Object> values, final NormalizedNode<?, ?> node,
            final DataSchemaContextNode<?> nodeContext, final List<QNamePredicate> nodePredicates,
            final Deque<QNameWithPredicate> path, final YangInstanceIdentifier current) {
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

        if (path.isEmpty()) {
            return;
        }

        final QNameWithPredicate next = path.pop();
        try {
            final QName qname = next.getQName();
            final DataSchemaContextNode<?> nextContext = verifyNotNull(nodeContext.getChild(qname));

            if (node instanceof DataContainerNode) {
                addValues(values, node, nextContext, qname, next.getQNamePredicates(), path, current);
                return;
            }
            if (node instanceof MapNode) {
                final MapNode map = (MapNode) node;
                Stream<MapEntryNode> entries = map.getValue().stream();
                if (!nodePredicates.isEmpty() && current != null) {
                    entries = entries.filter(createPredicate(nodePredicates, current));
                }

                entries.forEach(entry -> addValues(values, entry, nextContext, qname, next.getQNamePredicates(), path,
                    current));
            }
        } finally {
            path.push(next);
        }
    }

    private void addValues(final Set<Object> values, final NormalizedNode<?, ?> parent,
            final DataSchemaContextNode<?> nextContext, final QName nextQName,
            final List<QNamePredicate> nextPredicates, final Deque<QNameWithPredicate> iterator,
            final YangInstanceIdentifier current) {
        Optional<NormalizedNode<?, ?>> optChild = NormalizedNodes.findNode(parent, nextContext.getIdentifier());
        DataSchemaContextNode<?> childContext = nextContext;
        if (nextContext.isMixin()) {
            final DataSchemaContextNode<?> unmasked = verifyNotNull(nextContext.getChild(nextQName));
            if (!unmasked.isKeyedEntry()) {
                childContext = unmasked;
                optChild = NormalizedNodes.findNode(optChild, childContext.getIdentifier());
            }
        }
        if (optChild.isPresent()) {
            addValues(values, optChild.get(), childContext, nextPredicates, iterator, current);
        }
    }

    private Predicate<MapEntryNode> createPredicate(final List<QNamePredicate> predicates,
            final YangInstanceIdentifier current) {
        final Map<QName, Set<?>> keyValues = new HashMap<>();
        for (QNamePredicate predicate : predicates) {
            keyValues.put(predicate.getIdentifier(), getPathKeyExpressionValues(
                predicate.getPathKeyExpression(), current));
        }

        return entry -> {
            for (final Entry<QName, Object> entryKeyValue : entry.getIdentifier().getKeyValues().entrySet()) {
                final Set<?> allowedValues = keyValues.get(entryKeyValue.getKey());
                if (allowedValues != null && !allowedValues.contains(entryKeyValue.getValue())) {
                    return false;
                }
            }

            return true;
        };
    }

    private Set<?> getPathKeyExpressionValues(final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {
        NormalizedNode<?, ?> node = root;
        DataSchemaContextNode<?> context = rootContextNode;

        if (!current.isEmpty()) {
            final Iterator<PathArgument> it = current.getPathArguments().iterator();
            while (true) {
                final PathArgument arg = it.next();
                if (!it.hasNext()) {
                    break;
                }

                final DataSchemaContextNode<?> nextContext = context.getChild(arg);
                checkArgument(nextContext != null, "Failed to find context node for %s (%s is missing %s)", current,
                        context.getIdentifier(), arg);
                final Optional<NormalizedNode<?, ?>> nextNode = NormalizedNodes.getDirectChild(node, arg);
                if (!nextNode.isPresent()) {
                    LOG.debug("Node %s is not present", current);
                    return ImmutableSet.of();
                }

                context = nextContext;
                node = nextNode.get();
            }
        }

        final Deque<QNameWithPredicate> path = createPath(predicatePathKeyExpression);
        path.pollFirst();
        return computeValues(node, context, path, null);
    }

    private static Deque<QNameWithPredicate> createPath(final LeafRefPath path) {
        final Deque<QNameWithPredicate> ret = new ArrayDeque<>();
        path.getPathTowardsRoot().forEach(ret::push);
        return ret;
    }
}
