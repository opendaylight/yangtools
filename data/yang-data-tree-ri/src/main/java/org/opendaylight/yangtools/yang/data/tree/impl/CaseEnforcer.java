/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class CaseEnforcer implements Immutable {
    private static final class EnforcingMandatory extends CaseEnforcer {
        private final MandatoryLeafEnforcer enforcer;

        EnforcingMandatory(final ImmutableMap<NodeIdentifier, DataSchemaNode> children,
                final MandatoryLeafEnforcer enforcer) {
            super(children);
            this.enforcer = requireNonNull(enforcer);
        }

        @Override
        void enforceOnTreeNode(final NormalizedNode normalizedNode) {
            enforcer.enforceOnData(normalizedNode);
        }
    }

    private final ImmutableMap<NodeIdentifier, DataSchemaNode> children;

    CaseEnforcer(final ImmutableMap<NodeIdentifier, DataSchemaNode> children) {
        this.children = requireNonNull(children);
    }

    static CaseEnforcer forTree(final CaseSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final TreeType type = treeConfig.getTreeType();
        final Builder<NodeIdentifier, DataSchemaNode> childrenBuilder = ImmutableMap.builder();
        if (SchemaAwareApplyOperation.belongsToTree(type, schema)) {
            for (final DataSchemaNode child : schema.getChildNodes()) {
                if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                    childrenBuilder.put(NodeIdentifier.create(child.getQName()), child);
                }
            }
        }

        final ImmutableMap<NodeIdentifier, DataSchemaNode> children = childrenBuilder.build();
        if (children.isEmpty()) {
            return null;
        }
        final Optional<MandatoryLeafEnforcer> enforcer = MandatoryLeafEnforcer.forContainer(schema, treeConfig);
        return enforcer.isPresent() ? new EnforcingMandatory(children, enforcer.orElseThrow())
            : new CaseEnforcer(children);
    }

    final Set<Entry<NodeIdentifier, DataSchemaNode>> getChildEntries() {
        return children.entrySet();
    }

    final Set<NodeIdentifier> getChildIdentifiers() {
        return children.keySet();
    }

    void enforceOnTreeNode(final NormalizedNode normalizedNode) {
        // Default is no-op
    }
}
