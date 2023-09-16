/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. This is used by {@link EnforcingMandatory} in case of presence containers
 * with mandatory nodes, as it needs to tap into {@link SchemaAwareApplyOperation}'s operations, or subclassed
 * for the purposes of {@link StructuralContainerModificationStrategy}'s automatic lifecycle.
 */
sealed class ContainerModificationStrategy extends DataNodeContainerModificationStrategy<ContainerLike> {
    static final class EnforcingMandatory extends ContainerModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
                final MandatoryLeafEnforcer enforcer) {
            super(schemaNode, treeConfig);
            this.enforcer = requireNonNull(enforcer);
        }

        @Override
        void mandatoryVerifyValueChildren(final DistinctNodeContainer<?, ?> writtenValue) {
            enforcer.enforceOnData(writtenValue);
        }

        @Override
        protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            final var ret = super.applyMerge(modification, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }

        @Override
        protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
                final TreeNode currentMeta, final Version version) {
            final var ret = super.applyWrite(modification, newValue, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }

        @Override
        protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            final var ret = super.applyTouch(modification, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }
    }

    /**
     * Structural containers are special in that they appear when implied by child nodes and disappear whenever they are
     * empty. We could implement this as a subclass of {@link SchemaAwareApplyOperation}, but the automatic semantic
     * is quite different from all the other strategies. We create a {@link ContainerModificationStrategy} to tap into
     * that logic, but wrap it so we only call out into it. We do not use {@link PresenceContainerModificationStrategy}
     * because it enforces presence of mandatory leaves, which is not something we want here, as structural containers
     * are not root anchors for that validation.
     */
    static final class Structural extends ContainerModificationStrategy {
        private final ContainerNode emptyNode;

        Structural(final ContainerLike schema, final DataTreeConfiguration treeConfig) {
            super(schema, treeConfig);
            emptyNode = ImmutableNodes.containerNode(schema.getQName());
        }

        @Override
        Optional<? extends TreeNode> apply(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, currentMeta,
                version);
        }

        @Override
        TreeNode defaultTreeNode() {
            return defaultTreeNode(emptyNode);
        }
    }

    private static final NormalizedNodeContainerSupport<NodeIdentifier, ContainerNode> SUPPORT =
            new NormalizedNodeContainerSupport<>(ContainerNode.class, ImmutableContainerNodeBuilder::create,
                    ImmutableContainerNodeBuilder::create);

    ContainerModificationStrategy(final ContainerLike schemaNode, final DataTreeConfiguration treeConfig) {
        super(SUPPORT, schemaNode, treeConfig);
    }

    static ContainerModificationStrategy of(final ContainerSchemaNode schema, final DataTreeConfiguration treeConfig) {
        if (schema.isPresenceContainer()) {
            final var enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
            return enforcer != null ? new EnforcingMandatory(schema, treeConfig, enforcer)
                : new ContainerModificationStrategy(schema, treeConfig);
        }
        return new Structural(schema, treeConfig);
    }
}
