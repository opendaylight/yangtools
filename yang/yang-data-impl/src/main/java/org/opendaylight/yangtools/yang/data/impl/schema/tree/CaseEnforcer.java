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
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class CaseEnforcer implements Immutable {
    private final Map<NodeIdentifier, DataSchemaNode> children;

    private CaseEnforcer(final Map<NodeIdentifier, DataSchemaNode> children) {
        this.children = Preconditions.checkNotNull(children);
    }

    static CaseEnforcer forTree(final ChoiceCaseNode schema, final TreeType treeType) {
        final Builder<NodeIdentifier, DataSchemaNode> builder = ImmutableMap.builder();
        if (SchemaAwareApplyOperation.belongsToTree(treeType, schema)) {
            for (DataSchemaNode child : schema.getChildNodes()) {
                if (SchemaAwareApplyOperation.belongsToTree(treeType, child)) {
                    builder.put(NodeIdentifier.create(child.getQName()), child);
                }
            }
        }

        final Map<NodeIdentifier, DataSchemaNode> children = builder.build();
        return children.isEmpty() ? null : new CaseEnforcer(children);
    }

    Set<Entry<NodeIdentifier, DataSchemaNode>> getChildEntries() {
        return children.entrySet();
    }

    Set<NodeIdentifier> getChildIdentifiers() {
        return children.keySet();
    }
}
