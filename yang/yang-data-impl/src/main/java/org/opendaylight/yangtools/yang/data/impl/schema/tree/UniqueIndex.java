/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//FIXME: make indexes immutable/mutable like TreeNode/MutatableTreeNode
final class UniqueIndex implements TreeNodeIndex {
    private static final Logger LOG = LoggerFactory.getLogger(UniqueIndex.class);
    private final Map<List<Object>, MapEntryNode> indexMap;
    private final Map<NodeIdentifierWithPredicates, List<Object>> mapKeyToindexKey;
    private final Set<YangInstanceIdentifier> uniqueLeafIdentifiers;

    private UniqueIndex(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        this.uniqueLeafIdentifiers = Preconditions.checkNotNull(uniqueLeafIdentifiers);
        this.indexMap = new HashMap<>();
        this.mapKeyToindexKey = new HashMap<>();
    }

    private UniqueIndex(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers, final NormalizedNode<?, ?> data) {
        this(uniqueLeafIdentifiers);
        update(data);
    }

    Set<YangInstanceIdentifier> getUniqueLeafIdentifiers() {
        return uniqueLeafIdentifiers;
    }

    protected static TreeNodeIndex createEmpty(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        return new UniqueIndex(uniqueLeafIdentifiers);
    }

    protected static TreeNodeIndex createFromData(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers,
            final NormalizedNode<?, ?> data) {
        return new UniqueIndex(uniqueLeafIdentifiers, data);
    }

    @Override
    public void update(final NormalizedNode<?, ?> data) {
        Preconditions.checkArgument(data instanceof MapEntryNode || data instanceof MapNode);
        if (data instanceof MapNode) {
            updateAll((MapNode) data);
        } else if (data instanceof MapEntryNode) {
            updateEntry((MapEntryNode) data);
        } else {
            LOG.debug("Data {} must be either MapEntryNode or MapNode. Updating of unique index skipped.", data);
        }
    }

    private void updateEntry(final MapEntryNode data) {
        // :FIXME extract method and use ImmutableListBuilder
        final List<Object> indexKey = new ArrayList<>();
        for (final YangInstanceIdentifier id : uniqueLeafIdentifiers) {
            final Optional<NormalizedNode<?, ?>> findNode = NormalizedNodes.findNode(data, id);
            if (!findNode.isPresent() || findNode.get().getValue() == null) {
                LOG.debug("Node {} is missing. Updating of unique index is skipped for entry {}.", id, data);
                return;
            } else {
                indexKey.add(findNode.get().getValue());
            }
        }

        put(indexKey, data);
    }

    private boolean equalKeyEntries(final NormalizedNode<?, ?> indexData, final MapEntryNode data) {
        return data.getIdentifier().equals(indexData.getIdentifier());
    }

    @Override
    public void put(final List<Object> indexKey, final MapEntryNode data) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(indexKey);

        final Optional<NormalizedNode<?, ?>> indexData = get(indexKey);
        Preconditions
                .checkArgument(
                        !indexData.isPresent() || equalKeyEntries(indexData.get(), data),
                        "Node %s violates unique constraint. Stored node %s already contains the same value combination of leafs: %s",
                        data.getIdentifier(), indexData.isPresent() ? indexData.get().getIdentifier() : "",
                        uniqueLeafIdentifiers);

        final List<Object> originalIndexKey = mapKeyToindexKey.put(data.getIdentifier(), indexKey);
        indexMap.remove(originalIndexKey);
        indexMap.put(indexKey, data);
    }

    private void updateAll(final MapNode data) {
        for (final MapEntryNode mapEntry : data.getValue()) {
            update(mapEntry);
        }
    }

    @Override
    public Optional<NormalizedNode<?, ?>> get(final List<Object> indexKey) {
        return Optional.fromNullable(indexMap.get(indexKey));
    }

    void remove(final PathArgument id) {
        Preconditions.checkArgument(id instanceof NodeIdentifierWithPredicates, "Id must be instance of NodeIdentifierWithPredicates.");
        remove(mapKeyToindexKey.get(id));
   }

    @Override
    public void remove(final List<Object> indexKey) {
        Preconditions.checkNotNull(indexKey);
        final MapEntryNode removedEntry = indexMap.remove(indexKey);
        if(removedEntry != null) {
            mapKeyToindexKey.remove(removedEntry.getIdentifier());
        }
    }
}
