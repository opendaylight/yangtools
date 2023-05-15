/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.tree.impl.MandatoryDescendant.getAugIdentifierOfChild;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: would making this Serializable be useful (for Functions and similar?)
final class MandatoryLeafEnforcer implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(MandatoryLeafEnforcer.class);

    private final ImmutableList<MandatoryDescendant> mandatoryNodes;

    private MandatoryLeafEnforcer(final ImmutableList<MandatoryDescendant> mandatoryNodes) {
        this.mandatoryNodes = requireNonNull(mandatoryNodes);
    }

    static @Nullable MandatoryLeafEnforcer forContainer(final DataNodeContainer schema,
            final DataTreeConfiguration treeConfig) {
        if (!treeConfig.isMandatoryNodesValidationEnabled()) {
            return null;
        }

        final var builder = ImmutableList.<MandatoryDescendant>builder();
        final var isAugmentingNode = schema instanceof CopyableNode && ((CopyableNode) schema).isAugmenting();
        findMandatoryNodes(builder, YangInstanceIdentifier.empty(), schema, treeConfig.getTreeType(), isAugmentingNode);
        final var mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? null : new MandatoryLeafEnforcer(mandatoryNodes);
    }

    void enforceOnData(final NormalizedNode data) {
        mandatoryNodes.forEach(node -> node.enforceOnData(data));
    }

    void enforceOnTreeNode(final TreeNode tree) {
        enforceOnData(tree.getData());
    }

    private static void findMandatoryNodes(final Builder<MandatoryDescendant> builder,
        final YangInstanceIdentifier id, final DataNodeContainer schema, final TreeType type,
        final boolean augmentedSubtree) {
        for (final DataSchemaNode child : schema.getChildNodes()) {
            if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                if (child instanceof ContainerSchemaNode container) {
                    if (!container.isPresenceContainer()) {
                        if (!augmentedSubtree) {
                            // this container is not part of augmented subtree.
                            final boolean parentSchemaAugmenting = schema instanceof CopyableNode
                                && ((CopyableNode)schema).isAugmenting();
                            if (container.isAugmenting() && !parentSchemaAugmenting) {
                                // the container is augmenting, but the parent schema is not. Meaning this is the root
                                // of the augmentation (the augmented subtree starts here). The container has to be
                                // represented by AugmentationID and the whole subtree needs to be based on it.
                                final AugmentationSchemaNode aug = getAugIdentifierOfChild(schema, child);
                                findMandatoryNodes(builder, id.node(DataSchemaContextNode
                                    .augmentationIdentifierFrom(aug)).node(NodeIdentifier.create(container.getQName())),
                                    container, type, true);
                                continue;
                            }
                        }
                        // the container is either:
                        //    - not in an augmented subtree and not augmenting
                        //    - in an augmented subtree
                        // in both cases just append the NodeID to the ongoing ID and continue the search.
                        findMandatoryNodes(builder, id.node(NodeIdentifier.create(container.getQName())),
                            container, type, augmentedSubtree);
                    }
                } else {
                    boolean needEnforce = child instanceof MandatoryAware && ((MandatoryAware) child).isMandatory();
                    if (!needEnforce && child instanceof ElementCountConstraintAware) {
                        needEnforce = ((ElementCountConstraintAware) child).getElementCountConstraint()
                            .map(constraint -> {
                                final Integer min = constraint.getMinElements();
                                return min != null && min > 0;
                            })
                            .orElse(Boolean.FALSE);
                    }
                    if (needEnforce) {
                        final MandatoryDescendant desc = MandatoryDescendant.create(id, schema, child,
                            augmentedSubtree);
                        LOG.debug("Adding mandatory child {}", desc);
                        builder.add(desc);
                    }
                }
            }
        }
    }
}
