/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNodeIndex;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;

final class StrictUniqueIndexStrategy extends UniqueIndexStrategyBase {
    private final List<Set<YangInstanceIdentifier>> mapNodeUniqueLeafIdentifiers;

    StrictUniqueIndexStrategy(final Collection<UniqueConstraint> uniqueConstraints) {
        final ImmutableList.Builder<Set<YangInstanceIdentifier>> mapNodeUniqueLeafIdentifiers = ImmutableList
                .builder();
        for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
            mapNodeUniqueLeafIdentifiers.add(createUniqueConstraintIdList(uniqueConstraint));
        }
        this.mapNodeUniqueLeafIdentifiers = mapNodeUniqueLeafIdentifiers.build();
    }

    private static Set<YangInstanceIdentifier> createUniqueConstraintIdList(final UniqueConstraint uniqueConstraint) {
        final ImmutableSet.Builder<YangInstanceIdentifier> uniqueConstraintIdList = ImmutableSet.builder();
        final Collection<Relative> tag = uniqueConstraint.getTag();
        for (final Relative relative : tag) {
            final YangInstanceIdentifier id = createYangInstanceIdentifier(relative);
            uniqueConstraintIdList.add(id);
        }
        return uniqueConstraintIdList.build();
    }

    private static YangInstanceIdentifier createYangInstanceIdentifier(final Relative relative) {
        YangInstanceIdentifier id = YangInstanceIdentifier.EMPTY;
        for (final QName qname : relative.getPathFromRoot()) {
            id = id.node(qname);
        }
        return id;
    }

    @Override
    public Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> createIndexesFromData(
            final NormalizedNode<?, ?> data) {
        final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes = MapAdaptor.getDefaultInstance().initialSnapshot(1);
        for (final Set<YangInstanceIdentifier> uniqueLeafIdentifiers : mapNodeUniqueLeafIdentifiers) {
            indexes.put(uniqueLeafIdentifiers, MutableUniqueIndex
                    .createFromData(uniqueLeafIdentifiers, data).seal());
        }
        return indexes;
    }

    @Override
    public void updateIndexes(
            final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes,
            final NormalizedNode<?, ?> data) {
        for (final Entry<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexEntry : treeNodeIndexes
                .entrySet()) {
            final TreeNodeIndex<?, ?> treeNodeIndex = treeNodeIndexEntry.getValue();
            if (treeNodeIndex instanceof UniqueIndex || treeNodeIndex instanceof MutableUniqueIndex) {
                final MutableTreeNodeIndex<?, ?> mutable = treeNodeIndex.mutable();
                mutable.update(data);
                treeNodeIndexes.put(treeNodeIndexEntry.getKey(), mutable);
            }
        }
    }

    @Override
    public void removeFromIndexes(
            final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes, final PathArgument id) {
        for (final Entry<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexEntry : treeNodeIndexes
                .entrySet()) {
            final TreeNodeIndex<?, ?> treeNodeIndex = treeNodeIndexEntry.getValue();
            if (treeNodeIndex instanceof UniqueIndex || treeNodeIndex instanceof MutableUniqueIndex) {
                final MutableUniqueIndex mutable = ((UniqueIndex) treeNodeIndex).mutable();
                mutable.remove(id);
                treeNodeIndexes.put(treeNodeIndexEntry.getKey(), mutable);
            }
        }
    }

    @Override
    protected List<Set<YangInstanceIdentifier>> getUniqueConstraintsLeafIds() {
        return mapNodeUniqueLeafIdentifiers;
    }
}