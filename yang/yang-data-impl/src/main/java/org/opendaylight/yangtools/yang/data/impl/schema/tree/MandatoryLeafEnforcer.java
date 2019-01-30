/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
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

    private final ImmutableList<YangInstanceIdentifier> mandatoryNodes;

    private MandatoryLeafEnforcer(final ImmutableList<YangInstanceIdentifier> mandatoryNodes) {
        this.mandatoryNodes = requireNonNull(mandatoryNodes);
    }


    static Optional<MandatoryLeafEnforcer> forContainer(final DataNodeContainer schema,
            final DataTreeConfiguration treeConfig) {
        if (!treeConfig.isMandatoryNodesValidationEnabled()) {
            return Optional.empty();
        }

        final Builder<YangInstanceIdentifier> builder = ImmutableList.builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.EMPTY, schema, treeConfig.getTreeType());
        final ImmutableList<YangInstanceIdentifier> mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? Optional.empty() : Optional.of(new MandatoryLeafEnforcer(mandatoryNodes));
    }

    void enforceOnData(final NormalizedNode<?, ?> data) {
        for (final YangInstanceIdentifier id : mandatoryNodes) {
            checkArgument(NormalizedNodes.findNode(data, id).isPresent(),
                "Node %s is missing mandatory descendant %s", data.getIdentifier(), id);
        }
    }

    void enforceOnTreeNode(final TreeNode tree) {
        enforceOnData(tree.getData());
    }

    private static void findMandatoryNodes(final Builder<YangInstanceIdentifier> builder,
            final YangInstanceIdentifier id, final DataNodeContainer schema, final TreeType type) {
        for (final DataSchemaNode child : schema.getChildNodes()) {
            if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                if (child instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode container = (ContainerSchemaNode) child;
                    if (!container.isPresenceContainer()) {
                        findMandatoryNodes(builder, id.node(NodeIdentifier.create(child.getQName())), container, type);
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
                        final YangInstanceIdentifier childId = id.node(NodeIdentifier.create(child.getQName()));
                        LOG.debug("Adding mandatory child {}", childId);
                        builder.add(childId.toOptimized());
                    }
                }
            }
        }
    }
}
