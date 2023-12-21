/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

sealed class MapEntryModificationStrategy extends DataNodeContainerModificationStrategy<ListSchemaNode> {
    private static final class EnforcingMandatory extends MapEntryModificationStrategy {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ListSchemaNode schemaNode, final DataTreeConfiguration treeConfig,
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

    private static final NormalizedNodeContainerSupport<NodeIdentifierWithPredicates, MapEntryNode> SUPPORT =
            new NormalizedNodeContainerSupport<>(MapEntryNode.class, ImmutableMapEntryNodeBuilder::create,
                    ImmutableMapEntryNodeBuilder::new);

    MapEntryModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(SUPPORT, schema, treeConfig);
    }

    static @NonNull MapEntryModificationStrategy of(final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        final var enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
        return enforcer != null ? new EnforcingMandatory(schema, treeConfig, enforcer)
            : new MapEntryModificationStrategy(schema, treeConfig);
    }
}
