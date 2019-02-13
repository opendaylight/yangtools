/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Manual;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListEntryModificationStrategy extends DataNodeContainerModificationStrategy<ListSchemaNode> {
    private static final class EnforcingMandatory extends ListEntryModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ListSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
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

    private static final Manual<NodeIdentifierWithPredicates, MapEntryNode> SUPPORT = new Manual<>(MapEntryNode.class,
            ImmutableMapEntryNodeBuilder::create, ImmutableMapEntryNodeBuilder::create);

    ListEntryModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(SUPPORT, schema, treeConfig);
    }

    static @NonNull ListEntryModificationStrategy of(final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        final Optional<MandatoryLeafEnforcer> enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
        return enforcer.isPresent() ? new EnforcingMandatory(schema, treeConfig, enforcer.get())
                : new ListEntryModificationStrategy(schema, treeConfig);
    }
}
