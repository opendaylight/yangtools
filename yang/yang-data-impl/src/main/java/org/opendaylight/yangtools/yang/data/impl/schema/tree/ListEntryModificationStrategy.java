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

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListEntryModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ListSchemaNode> {
    private static final class EnforcingMandatory extends ListEntryModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ListSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
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

    ListEntryModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(schema, MapEntryNode.class, treeConfig);
    }

    static ListEntryModificationStrategy of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final Optional<MandatoryLeafEnforcer> enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
        return enforcer.isPresent() ? new EnforcingMandatory(schema, treeConfig, enforcer.get())
                : new ListEntryModificationStrategy(schema, treeConfig);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof MapEntryNode);
        return ImmutableMapEntryNodeBuilder.create((MapEntryNode) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof MapEntryNode);
        return ImmutableMapEntryNodeBuilder.create().withNodeIdentifier(((MapEntryNode) original).getIdentifier())
                .build();
    }
}