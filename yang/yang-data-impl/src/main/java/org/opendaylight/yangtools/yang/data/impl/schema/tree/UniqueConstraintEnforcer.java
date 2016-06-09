/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: would making this Serializable be useful (for Functions and similar?)
abstract class UniqueConstraintEnforcer implements Immutable {
    private static final class NoOp extends UniqueConstraintEnforcer {
        @Override
        protected void enforceOnTreeNode(final TreeNode tree) {
            // Intentional no-op
        }

        @Override
        protected void enforceOnTreeNode(final NormalizedNode<?, ?> normalizedNode) {
            // Intentional no-op
        }
    }

    private static final class Strict extends UniqueConstraintEnforcer {
        private final Map<List<YangInstanceIdentifier>, UniqueIndex> uniqueIndexes;

        private Strict(final Collection<UniqueConstraint> uniqueConstraints) {
            final ImmutableMap.Builder<List<YangInstanceIdentifier>, UniqueIndex> uniqueIndexesBuilder = ImmutableMap
                    .builder();
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                final UniqueIndex uniqueIndex = new UniqueIndex(uniqueConstraint);
                uniqueIndexesBuilder.put(uniqueIndex.getUniqueLeafIdentifiers(), uniqueIndex);
            }
            this.uniqueIndexes = uniqueIndexesBuilder.build();
        }

        @Override
        protected void enforceOnTreeNode(final TreeNode tree) {
            enforceOnTreeNode(tree.getData());
        }

        @Override
        protected void enforceOnTreeNode(final NormalizedNode<?, ?> data) {
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(UniqueConstraintEnforcer.class);
    private static final UniqueConstraintEnforcer NOOP_ENFORCER = new NoOp();

    protected abstract void enforceOnTreeNode(final TreeNode tree);

    protected abstract void enforceOnTreeNode(final NormalizedNode<?, ?> normalizedNode);

    static UniqueConstraintEnforcer forList(final ListSchemaNode schema, final TreeType type) {
        switch (type) {
        case CONFIGURATION:
            final Collection<UniqueConstraint> uniqueConstraints = schema.getUniqueConstraints();
            return uniqueConstraints.isEmpty() ? NOOP_ENFORCER : new Strict(uniqueConstraints);
        case OPERATIONAL:
            return NOOP_ENFORCER;
        default:
            throw new UnsupportedOperationException(String.format("Not supported tree type %s", type));
        }
    }
}
