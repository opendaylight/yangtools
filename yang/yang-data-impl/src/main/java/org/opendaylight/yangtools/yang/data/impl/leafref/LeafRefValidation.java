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

public final class LeafRefValidation {
    private static final Logger LOG = LoggerFactory.getLogger(LeafRefValidation.class);
    private static final String FAILED = " -> FAILED";
    private static final String SUCCESS = " -> OK";

    private final Set<LeafRefContext> validatedLeafRefCtx = new HashSet<>();
    private final List<String> errorsMessages = new ArrayList<>();
    private final DataSchemaContextNode<?> rootContext;
    private final NormalizedNode<?, ?> root;

    private LeafRefValidation(final DataSchemaContextNode<?> rootContext, final NormalizedNode<?, ?> root) {
        this.rootContext = requireNonNull(rootContext);
        this.root = requireNonNull(root);
    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        final Optional<NormalizedNode<?, ?>> root = tree.getRootNode().getDataAfter();
        if (root.isPresent()) {
            new LeafRefValidation(DataSchemaContextTree.from(rootLeafRefCtx.getSchemaContext()).getRoot(), root.get())
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
            validateMapNodeData((MapNode) node, referencingCtx, referencingCtx, modificationType, current);
        }
        // FIXME: check UnkeyedListNode case
    }

    private void validateChoiceNodeData(final ChoiceNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (final DataContainerChild<? extends PathArgument, ?> dataContainerChild : node.getValue()) {
            final QName qname = dataContainerChild.getNodeType();
            final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                    : findReferencedByCtxUnderChoice(referencedByCtx, qname);
            final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                    : findReferencingCtxUnderChoice(referencingCtx, qname);

            if (childReferencedByCtx != null || childReferencingCtx != null) {
                validateNodeData(dataContainerChild, childReferencedByCtx, childReferencingCtx, modificationType,
                    current.node(dataContainerChild.getIdentifier()));
            }
        }
    }

    private void validateDataContainerNodeData(final DataContainerNode<?> node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (final DataContainerChild<? extends PathArgument, ?> child : node.getValue()) {
            if (child instanceof AugmentationNode) {
                validateNodeData(child, referencedByCtx, referencingCtx, modificationType,
                    current.node(child.getIdentifier()));
                return;
            }

            final QName qname = child.getNodeType();
            final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                    : referencedByCtx.getReferencedChildByName(qname);
            final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                    : referencingCtx.getReferencingChildByName(qname);

            if (childReferencedByCtx != null || childReferencingCtx != null) {
                validateNodeData(child, childReferencedByCtx, childReferencingCtx, modificationType,
                    current.node(child.getIdentifier()));
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

    private void validateMapNodeData(final MapNode node, final LeafRefContext referencedByCtx,
            final LeafRefContext referencingCtx, final ModificationType modificationType,
            final YangInstanceIdentifier current) {
        for (final MapEntryNode mapEntry : node.getValue()) {
            final YangInstanceIdentifier mapEntryYangInstanceIdentifier = current.node(mapEntry.getIdentifier());
            for (final DataContainerChild<? extends PathArgument, ?> mapEntryNode : mapEntry.getValue()) {
                if (mapEntryNode instanceof AugmentationNode) {
                    validateNodeData(mapEntryNode, referencedByCtx, referencingCtx, modificationType, current
                        .node(mapEntryNode.getIdentifier()));
                    return;
                }

                final QName qname = mapEntryNode.getNodeType();
                final LeafRefContext childReferencedByCtx = referencedByCtx == null ? null
                    : referencedByCtx.getReferencedChildByName(qname);
                final LeafRefContext childReferencingCtx = referencingCtx == null ? null
                    : referencingCtx.getReferencingChildByName(qname);

                if (childReferencedByCtx != null || childReferencingCtx != null) {
                    validateNodeData(mapEntryNode, childReferencedByCtx, childReferencingCtx, modificationType,
                            mapEntryYangInstanceIdentifier.node(mapEntryNode.getIdentifier()));
                }
            }
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
        return computeValues(root, rootContext, createPath(context.getLeafRefNodePath()), null);
    }

    private void validateLeafRefNodeData(final NormalizedNode<?, ?> leaf, final LeafRefContext referencingCtx,
            final ModificationType modificationType, final YangInstanceIdentifier current) {
        final Set<Object> values = computeValues(root, rootContext,
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
            final Deque<QNameWithPredicate> path, final YangInstanceIdentifier current) {
        final HashSet<Object> values = new HashSet<>();
        addValues(values, node, nodeContext, ImmutableList.of(), path, current);
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

        final QNameWithPredicate next = path.peek();
        if (next == null) {
            return;
        }

        if (node instanceof DataContainerNode) {
            final QName qname = next.getQName();
            final DataSchemaContextNode<?> nextContext = verifyNotNull(nodeContext.getChild(qname),
                "Context %s does not have a child matching %s", nodeContext, qname);
            final DataContainerNode<?> dataContainerNode = (DataContainerNode<?>) node;
            final Optional<DataContainerChild<?, ?>> optChild = dataContainerNode.getChild(nextContext.getIdentifier());
            if (optChild.isPresent()) {
                addNextValues(values, optChild.get(), nextContext, next.getQNamePredicates(), path, current);
            } else {
                LOG.debug("Failed to find child {} in container {}", nextContext.getIdentifier(), dataContainerNode);
            }
        } else if (node instanceof MapNode) {
            final DataSchemaContextNode<?> unmasked = verifyNotNull(nodeContext.getChild(node.getIdentifier()
                .getNodeType()));
            final QName qname = next.getQName();
            final DataSchemaContextNode<?> nextContext = verifyNotNull(unmasked.getChild(qname),
                "Context %s does not have a child matching %s", nodeContext, qname);

            Stream<MapEntryNode> entries = ((MapNode) node).getValue().stream();
            if (!nodePredicates.isEmpty() && current != null) {
                entries = entries.filter(createMapEntryPredicate(nodePredicates, current));
            }

            entries.forEach(mapEntryNode -> {
                final Optional<DataContainerChild<?, ?>> optChild = mapEntryNode.getChild(nextContext.getIdentifier());
                if (optChild.isPresent()) {
                    addNextValues(values, optChild.get(), nextContext, next.getQNamePredicates(), path, current);
                } else {
                    LOG.debug("Failed to find child {} in entry {}", nextContext.getIdentifier(), mapEntryNode);
                }
            });
        } else {
            throw new IllegalStateException("Unhandled node " + node);
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
            final DataSchemaContextNode<?> nodeContext, final List<QNamePredicate> nodePredicates,
            final Deque<QNameWithPredicate> path, final YangInstanceIdentifier current) {
        if (!nodeContext.isMixin() || node instanceof MapNode) {
            final QNameWithPredicate element = path.pop();
            try {
                addValues(values, node, nodeContext, nodePredicates, path, current);
            } finally {
                path.push(element);
            }
        } else {
            addValues(values, node, nodeContext, nodePredicates, path, current);
        }
    }

    private Set<?> getPathKeyExpressionValues(final LeafRefPath predicatePathKeyExpression,
            final YangInstanceIdentifier current) {
        NormalizedNode<?, ?> node = root;
        DataSchemaContextNode<?> context = rootContext;
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
