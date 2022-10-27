/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
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

    private final ImmutableList<MandatoryDescendant> mandatoryNodes;

    private MandatoryLeafEnforcer(final ImmutableList<MandatoryDescendant> mandatoryNodes) {
        this.mandatoryNodes = requireNonNull(mandatoryNodes);
    }

    static Optional<MandatoryLeafEnforcer> forContainer(final DataNodeContainer schema,
            final DataTreeConfiguration treeConfig) {
        if (!treeConfig.isMandatoryNodesValidationEnabled()) {
            return Optional.empty();
        }

        final Builder<MandatoryDescendant> builder = ImmutableList.builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.empty(), schema, treeConfig.getTreeType());
        final ImmutableList<MandatoryDescendant> mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? Optional.empty() : Optional.of(new MandatoryLeafEnforcer(mandatoryNodes));
    }

    void enforceOnData(final NormalizedNode data) {
        mandatoryNodes.forEach(node -> node.enforceOnData(data));
    }

    void enforceOnTreeNode(final TreeNode tree) {
        enforceOnData(tree.getData());
    }

    private static void findMandatoryNodes(final Builder<MandatoryDescendant> builder, final YangInstanceIdentifier id,
            final DataNodeContainer schema, final TreeType type) {
        for (final DataSchemaNode child : schema.getChildNodes()) {
            if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                if (child instanceof ContainerSchemaNode container) {
                    if (!container.isPresenceContainer()) {
                        // the container is either:
                        //    - not in an augmented subtree and not augmenting
                        //    - in an augmented subtree
                        // in both cases just append the NodeID to the ongoing ID and continue the search.
                        findMandatoryNodes(builder, id.node(NodeIdentifier.create(container.getQName())), container,
                            type);
                    }
                } else {
                    boolean needEnforce = child instanceof MandatoryAware aware && aware.isMandatory();
                    if (!needEnforce && child instanceof ElementCountConstraintAware aware) {
                        needEnforce = aware.getElementCountConstraint()
                            .map(constraint -> {
                                final Integer min = constraint.getMinElements();
                                return min != null && min > 0;
                            })
                            .orElse(Boolean.FALSE);
                    }
                    if (needEnforce) {
                        final MandatoryDescendant desc = MandatoryDescendant.create(id, schema, child);
                        LOG.debug("Adding mandatory child {}", desc);
                        builder.add(desc);
                    }
                }
            }
        }
    }
}
