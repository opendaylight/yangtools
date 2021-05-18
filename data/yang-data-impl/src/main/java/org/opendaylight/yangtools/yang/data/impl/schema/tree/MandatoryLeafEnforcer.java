/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.spi.tree.TreeNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: would making this Serializable be useful (for Functions and similar?)
final class MandatoryLeafEnforcer implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(MandatoryLeafEnforcer.class);

    // introduced Map instead of List to support the enforcing mechanism for both versions of mandatory leafs which
    // were added via augmentation. Related issue: YANGTOOLS-1276
    private final ImmutableMap<YangInstanceIdentifier, YangInstanceIdentifier> directToAugMandatoryNodes;

    private MandatoryLeafEnforcer(final ImmutableMap<YangInstanceIdentifier, YangInstanceIdentifier>
        directToAugMandatoryNodes) {
        this.directToAugMandatoryNodes = requireNonNull(directToAugMandatoryNodes);
    }

    static Optional<MandatoryLeafEnforcer> forContainer(final DataNodeContainer schema,
            final DataTreeConfiguration treeConfig) {
        if (!treeConfig.isMandatoryNodesValidationEnabled()) {
            return Optional.empty();
        }

        final Builder<YangInstanceIdentifier, YangInstanceIdentifier> builder = ImmutableMap.builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.empty(), schema, treeConfig.getTreeType());
        final ImmutableMap<YangInstanceIdentifier, YangInstanceIdentifier> mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? Optional.empty() : Optional.of(new MandatoryLeafEnforcer(mandatoryNodes));
    }

    /**
     * Due to an issue with augmented mandatory nodes(YANGTOOLS-1276), the enforcing is done in 2 steps here.
     * - first seek the mandatory node as a direct child of the data node - using his NodeIdentifier
     * - in case the mandatory node came from augmentation, try looking for his AugmentationIdentifier
     */
    void enforceOnData(final NormalizedNode data) {
        for (Map.Entry<YangInstanceIdentifier, YangInstanceIdentifier> id : directToAugMandatoryNodes.entrySet()) {
            if (NormalizedNodes.findNode(data, id.getKey()).isEmpty()) {
                YangInstanceIdentifier augmentedId = id.getValue();
                if (!augmentedId.isEmpty() && NormalizedNodes.findNode(data, augmentedId).isPresent()) {
                    return;
                }
                throw new IllegalArgumentException(String.format("Node %s is missing mandatory descendant %s",
                    data.getIdentifier(), id.getKey()));
            }
        }
    }

    void enforceOnTreeNode(final TreeNode tree) {
        enforceOnData(tree.getData());
    }

    private static void findMandatoryNodes(final Builder<YangInstanceIdentifier, YangInstanceIdentifier>
        directToAugmentedChildBuilder, final YangInstanceIdentifier id, final DataNodeContainer schema,
        final TreeType type) {
        for (final DataSchemaNode child : schema.getChildNodes()) {
            if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                if (child instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode container = (ContainerSchemaNode) child;
                    if (!container.isPresenceContainer()) {
                        findMandatoryNodes(directToAugmentedChildBuilder,
                            id.node(NodeIdentifier.create(child.getQName())), container, type);
                    }
                } else {
                    boolean needEnforce = child instanceof MandatoryAware && ((MandatoryAware) child).isMandatory();
                    if (!needEnforce && child instanceof ElementCountConstraintAware) {
                        needEnforce = ((ElementCountConstraintAware) child)
                                .getElementCountConstraint().map(constraint -> {
                                    final Integer min = constraint.getMinElements();
                                    return min != null && min > 0;
                                }).orElse(Boolean.FALSE).booleanValue();
                    }
                    if (needEnforce) {
                        final YangInstanceIdentifier directChildId = id.node(NodeIdentifier.create(child.getQName()))
                            .toOptimized();
                        if (child.isAugmenting()) {
                            Collection<? extends AugmentationSchemaNode> augmentations =
                                ((AugmentationTarget) schema).getAvailableAugmentations();
                            for (AugmentationSchemaNode augmentation : augmentations) {
                                if (augmentation.findDataChildByName(child.getQName()).isPresent()) {
                                    final Set<QName> augQnames =
                                        augmentation.getChildNodes().stream().map(DataSchemaNode::getQName)
                                            .collect(Collectors.toSet());

                                    final YangInstanceIdentifier augmentedChildId = id.node(AugmentationIdentifier
                                        .create(augQnames)).toOptimized();
                                    LOG.debug("Adding both direct and augmented versions of the mandatory child: "
                                            + "direct {}, augmented: {}", directChildId, augmentedChildId);
                                    directToAugmentedChildBuilder.put(directChildId, augmentedChildId);
                                    break;
                                }
                            }
                        } else {
                            LOG.debug("Adding direct mandatory child {}", directChildId);
                            directToAugmentedChildBuilder.put(directChildId, YangInstanceIdentifier.empty());
                        }
                    }
                }
            }
        }
    }
}
