/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class CaseEnforcer implements Immutable {
    private final Map<NodeIdentifier, DataSchemaNode> children;
    private final MandatoryLeafEnforcer enforcer;

    private CaseEnforcer(final Map<NodeIdentifier, DataSchemaNode> children, final MandatoryLeafEnforcer enforcer) {
        this.children = Preconditions.checkNotNull(children);
        this.enforcer = Preconditions.checkNotNull(enforcer);
    }

    static CaseEnforcer forTree(final ChoiceCaseNode schema, final DataTreeConfiguration treeConfig) {
        final TreeType type = treeConfig.getTreeType();
        final Builder<NodeIdentifier, DataSchemaNode> builder = ImmutableMap.builder();
        if (SchemaAwareApplyOperation.belongsToTree(type, schema)) {
            for (final DataSchemaNode child : schema.getChildNodes()) {
                if (SchemaAwareApplyOperation.belongsToTree(type, child)) {
                    builder.put(NodeIdentifier.create(child.getQName()), child);
                }
            }
        }

        final Map<NodeIdentifier, DataSchemaNode> children = builder.build();
        return children.isEmpty() ? null : new CaseEnforcer(children, MandatoryLeafEnforcer.forContainer(schema,
                treeConfig));
    }

    Set<Entry<NodeIdentifier, DataSchemaNode>> getChildEntries() {
        return children.entrySet();
    }

    Set<NodeIdentifier> getChildIdentifiers() {
        return children.keySet();
    }

    void enforceOnTreeNode(final NormalizedNode<?, ?> normalizedNode) {
        enforcer.enforceOnData(normalizedNode);
    }
}
