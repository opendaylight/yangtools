/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Manual;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. This is used by {@link EnforcingMandatory} in case of presence containers
 * with mandatory nodes, as it needs to tap into {@link SchemaAwareApplyOperation}'s operations, or subclassed
 * for the purposes of {@link StructuralContainerModificationStrategy}'s automatic lifecycle.
 */
class ContainerModificationStrategy extends DataNodeContainerModificationStrategy<ContainerSchemaNode> {
    private static final class EnforcingMandatory extends ContainerModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
                final MandatoryLeafEnforcer enforcer) {
            super(schemaNode, treeConfig);
            this.enforcer = requireNonNull(enforcer);
        }

        @Override
        void mandatoryVerifyValueChildren(final NormalizedNode<?, ?> writtenValue) {
            enforcer.enforceOnData(writtenValue);
        }

        @Override
        protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            final TreeNode ret = super.applyMerge(modification, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }

        @Override
        protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode<?, ?> newValue,
                final Optional<TreeNode> currentMeta, final Version version) {
            final TreeNode ret = super.applyWrite(modification, newValue, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }

        @Override
        protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            final TreeNode ret = super.applyTouch(modification, currentMeta, version);
            enforcer.enforceOnTreeNode(ret);
            return ret;
        }
    }

    private static final Manual<NodeIdentifier, ContainerNode> SUPPORT = new Manual<>(ContainerNode.class,
            ImmutableContainerNodeBuilder::create, ImmutableContainerNodeBuilder::create);

    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        this(SUPPORT, schemaNode, treeConfig);
    }

    ContainerModificationStrategy(final NormalizedNodeContainerSupport<NodeIdentifier, ContainerNode> support,
            final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(support, schemaNode, treeConfig);
    }

    static ModificationApplyOperation of(final ContainerSchemaNode schema, final DataTreeConfiguration treeConfig) {
        if (schema.isPresenceContainer()) {
            final Optional<MandatoryLeafEnforcer> enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
            return enforcer.isPresent() ? new EnforcingMandatory(schema, treeConfig, enforcer.get())
                    : new ContainerModificationStrategy(schema, treeConfig);
        }

        return new StructuralContainerModificationStrategy(schema, treeConfig);
    }
}
