/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeConfig;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class UniqueConstraintEnforcer implements Immutable {
    private static final class NoOp extends UniqueConstraintEnforcer {
        @Override
        protected Map<List<YangInstanceIdentifier>, TreeNodeIndex> createIndexesFromData(
                final NormalizedNode<?, ?> newValue) {
            return ImmutableMap.of();
        }

        @Override
        protected void updateIndexes(final Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
                final NormalizedNode<?, ?> data) {
            // Intentional no-op
        }

        @Override
        protected void removeFromIndexes(final Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
                final PathArgument id) {
            // Intentional no-op
        }

        @Override
        protected List<List<YangInstanceIdentifier>> getUniqueConstraintsLeafIds() {
            return ImmutableList.of();
        }
    }

    private static final class Strict extends UniqueConstraintEnforcer {
        private final List<List<YangInstanceIdentifier>> mapNodeUniqueLeafIdentifiers;

        private Strict(final Collection<UniqueConstraint> uniqueConstraints) {
            final ImmutableList.Builder<List<YangInstanceIdentifier>> mapNodeUniqueLeafIdentifiers = ImmutableList
                    .builder();
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                mapNodeUniqueLeafIdentifiers.add(createUniqueConstraintIdList(uniqueConstraint));
            }
            this.mapNodeUniqueLeafIdentifiers = mapNodeUniqueLeafIdentifiers.build();
        }

        private static List<YangInstanceIdentifier> createUniqueConstraintIdList(final UniqueConstraint uniqueConstraint) {
            final ImmutableList.Builder<YangInstanceIdentifier> uniqueConstraintIdList = ImmutableList.builder();
            final Collection<Relative> tag = uniqueConstraint.getTag();
            for (final Relative relative : tag) {
                final YangInstanceIdentifier id = createYangInstanceIdentifier(relative);
                uniqueConstraintIdList.add(id);
            }
            return uniqueConstraintIdList.build();
        }

        private static YangInstanceIdentifier createYangInstanceIdentifier(final Relative relative) {
            YangInstanceIdentifier id = YangInstanceIdentifier.EMPTY;
            // :FIXME find out how to concert this safely
            for (final QName qname : relative.getPathFromRoot()) {
                id = id.node(qname);
            }
            return id;
        }

        @Override
        protected Map<List<YangInstanceIdentifier>, TreeNodeIndex> createIndexesFromData(final NormalizedNode<?, ?> data) {
            final ImmutableMap.Builder<List<YangInstanceIdentifier>, TreeNodeIndex> indexesBuilder = ImmutableMap
                    .builder();
            for (final List<YangInstanceIdentifier> uniqueLeafIdentifiers : mapNodeUniqueLeafIdentifiers) {
                indexesBuilder.put(uniqueLeafIdentifiers, UniqueIndex.createFromData(uniqueLeafIdentifiers, data));
            }
            return indexesBuilder.build();
        }

        @Override
        protected void updateIndexes(final Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
                final NormalizedNode<?, ?> data) {
            for (final Entry<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexEntry : treeNodeIndexes
                    .entrySet()) {
                final TreeNodeIndex treeNodeIndex = treeNodeIndexEntry.getValue();
                if (treeNodeIndex instanceof UniqueIndex) {
                    treeNodeIndex.update(data);
                }
            }
        }

        @Override
        protected void removeFromIndexes(final Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
                final PathArgument id) {
        }

        @Override
        protected List<List<YangInstanceIdentifier>> getUniqueConstraintsLeafIds() {
            return mapNodeUniqueLeafIdentifiers;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(UniqueConstraintEnforcer.class);
    private static final UniqueConstraintEnforcer NOOP_ENFORCER = new NoOp();

    protected abstract Map<List<YangInstanceIdentifier>, TreeNodeIndex> createIndexesFromData(
            final NormalizedNode<?, ?> newValue);

    protected abstract void updateIndexes(Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
            NormalizedNode<?, ?> data);

    protected abstract void removeFromIndexes(Map<List<YangInstanceIdentifier>, TreeNodeIndex> treeNodeIndexes,
            PathArgument id);

    static UniqueConstraintEnforcer forList(final ListSchemaNode schema, final TreeConfig config) {
        switch (config.getTreeType()) {
        case CONFIGURATION:
            final Collection<UniqueConstraint> uniqueConstraints = schema.getUniqueConstraints();
            return !config.isUniqueIndexEnabled() || uniqueConstraints.isEmpty() ? NOOP_ENFORCER : new Strict(uniqueConstraints);
        case OPERATIONAL:
            return NOOP_ENFORCER;
        default:
            throw new UnsupportedOperationException(String.format("Not supported tree type %s", config.getTreeType()));
        }
    }

    abstract protected List<List<YangInstanceIdentifier>> getUniqueConstraintsLeafIds();
}
