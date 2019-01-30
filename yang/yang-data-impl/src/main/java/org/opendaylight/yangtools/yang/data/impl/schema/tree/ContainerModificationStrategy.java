/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. This is used by {@link EnforcingMandatory} in case of presence containers
 * with mandatory nodes, as it needs to tap into {@link SchemaAwareApplyOperation}'s operations, or wrapped through
 * {@link AutomaticLifecycleMixin} when the container is a structural one.
 */
class ContainerModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ContainerSchemaNode> {
    private static final class EnforcingMandatory extends ContainerModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
                final MandatoryLeafEnforcer enforcer) {
            super(schemaNode, treeConfig);
            this.enforcer = requireNonNull(enforcer);
        }

        @Override
        void verifyStructure(final NormalizedNode<?, ?> writtenValue, final boolean verifyChildren) {
            super.verifyStructure(writtenValue, verifyChildren);
            if (verifyChildren) {
                enforcer.enforceOnData(writtenValue);
            }
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

    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(schemaNode, ContainerNode.class, treeConfig);
    }

    static ModificationApplyOperation of(final ContainerSchemaNode schema, final DataTreeConfiguration treeConfig) {
        if (schema.isPresenceContainer()) {
            final Optional<MandatoryLeafEnforcer> enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
            return enforcer.isPresent() ? new EnforcingMandatory(schema, treeConfig, enforcer.get())
                    : new ContainerModificationStrategy(schema, treeConfig);
        }

        return new AutomaticLifecycleMixin(new ContainerModificationStrategy(schema, treeConfig),
            ImmutableNodes.containerNode(schema.getQName()));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected final DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create((ContainerNode) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        Preconditions.checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(((ContainerNode) original).getIdentifier())
                .build();
    }
}
