/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: would making this Serializable be useful (for Functions and similar?)
abstract class MandatoryLeafEnforcer implements Immutable {
    private static final class Strict extends MandatoryLeafEnforcer {
        private final Collection<YangInstanceIdentifier> mandatoryNodes;

        Strict(final Collection<YangInstanceIdentifier> mandatoryNodes) {
            this.mandatoryNodes = Preconditions.checkNotNull(mandatoryNodes);
        }

        @Override
        void enforceOnData(final NormalizedNode<?, ?> data) {
            for (final YangInstanceIdentifier id : mandatoryNodes) {
                final Optional<NormalizedNode<?, ?>> descandant = NormalizedNodes.findNode(data, id);
                Preconditions.checkArgument(descandant.isPresent(), "Node %s is missing mandatory descendant %s",
                        data.getIdentifier(), id);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(MandatoryLeafEnforcer.class);
    private static final MandatoryLeafEnforcer NOOP_ENFORCER = new MandatoryLeafEnforcer() {
        @Override
        void enforceOnData(final NormalizedNode<?, ?> normalizedNode) {
            // Intentional no-op
        }
    };

    final void enforceOnTreeNode(final TreeNode tree) {
        enforceOnData(tree.getData());
    }

    abstract void enforceOnData(final NormalizedNode<?, ?> normalizedNode);

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
                    final ConstraintDefinition constraints = child.getConstraints();
                    final Integer minElements = constraints.getMinElements();
                    if (constraints.isMandatory() || minElements != null && minElements.intValue() > 0) {
                        final YangInstanceIdentifier childId = id.node(NodeIdentifier.create(child.getQName()));
                        LOG.debug("Adding mandatory child {}", childId);
                        builder.add(childId.toOptimized());
                    }
                }
            }
        }
    }

    static MandatoryLeafEnforcer forContainer(final DataNodeContainer schema, final DataTreeConfiguration treeConfig) {
        if (!treeConfig.isMandatoryNodesValidationEnabled()) {
            return NOOP_ENFORCER;
        }

        final Builder<YangInstanceIdentifier> builder = ImmutableList.builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.EMPTY, schema, treeConfig.getTreeType());
        final Collection<YangInstanceIdentifier> mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? NOOP_ENFORCER : new Strict(mandatoryNodes);
    }
}
