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
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexKey;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNodeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MutableUniqueIndex implements MutableTreeNodeIndex<UniqueIndexKey, MapEntryNode> {
    private static final Logger LOG = LoggerFactory.getLogger(MutableUniqueIndex.class);
    private final Map<UniqueIndexKey, MapEntryNode> indexMap;
    private final Map<NodeIdentifierWithPredicates, UniqueIndexKey> mapKeyToindexKey;
    private final Set<YangInstanceIdentifier> uniqueLeafIdentifiers;
    private final List<Operation> indexChanges = new ArrayList<>();
    private boolean sealed = false;

    private MutableUniqueIndex(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        this.uniqueLeafIdentifiers = Preconditions.checkNotNull(uniqueLeafIdentifiers);
        this.indexMap = MapAdaptor.getDefaultInstance().initialSnapshot(1);
        this.mapKeyToindexKey = MapAdaptor.getDefaultInstance().initialSnapshot(1);
    }

    private MutableUniqueIndex(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers, final NormalizedNode<?, ?> data) {
        this(uniqueLeafIdentifiers);
        update(data);
    }

    MutableUniqueIndex(final Map<UniqueIndexKey, MapEntryNode> indexMap,
            final Map<NodeIdentifierWithPredicates, UniqueIndexKey> mapKeyToindexKey,
            final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        Preconditions.checkNotNull(indexMap);
        Preconditions.checkNotNull(mapKeyToindexKey);
        this.indexMap = MapAdaptor.getDefaultInstance().takeSnapshot(indexMap);
        this.mapKeyToindexKey = MapAdaptor.getDefaultInstance().takeSnapshot(mapKeyToindexKey);
        this.uniqueLeafIdentifiers = Preconditions.checkNotNull(uniqueLeafIdentifiers);
    }

    Set<YangInstanceIdentifier> getUniqueLeafIdentifiers() {
        return uniqueLeafIdentifiers;
    }

    protected static MutableUniqueIndex createEmpty(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        return new MutableUniqueIndex(uniqueLeafIdentifiers);
    }

    protected static MutableUniqueIndex createFromData(final Set<YangInstanceIdentifier> uniqueLeafIdentifiers,
            final NormalizedNode<?, ?> data) {
        return new MutableUniqueIndex(uniqueLeafIdentifiers, data);
    }

    @Override
    public void update(final NormalizedNode<?, ?> data) {
        Preconditions.checkState(!sealed);
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
        Preconditions.checkState(!sealed);
        final ImmutableMap.Builder<YangInstanceIdentifier, Object> indexKeyValue = new ImmutableMap.Builder<>();
        for (final YangInstanceIdentifier id : uniqueLeafIdentifiers) {
            final Optional<NormalizedNode<?, ?>> findNode = NormalizedNodes.findNode(data, id);
            if (!findNode.isPresent() || findNode.get().getValue() == null) {
                LOG.debug("Node {} is missing. Updating of unique index is skipped for entry {}.", id, data);
                return;
            } else {
                indexKeyValue.put(id, findNode.get().getValue());
            }
        }

        put(new UniqueIndexKey(indexKeyValue.build()), data);
    }

    private boolean equalKeyEntries(final MapEntryNode indexData, final MapEntryNode data) {
        Preconditions.checkState(!sealed);
        return data.getIdentifier().equals(indexData.getIdentifier());
    }

    @Override
    public void put(final UniqueIndexKey indexKey, final MapEntryNode data) {
        Preconditions.checkState(!sealed);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(indexKey);
        addPutOperation(indexKey, data);
    }

    private void addPutOperation(final UniqueIndexKey indexKey, final MapEntryNode data) {
        indexChanges.add(new Operation(OperationType.PUT, indexKey, data));
    }

    private void putToIndex(final UniqueIndexKey indexKey, final MapEntryNode data) {
        final Optional<MapEntryNode> indexData = get(indexKey);
        Preconditions
                .checkArgument(
                        !indexData.isPresent() || equalKeyEntries(indexData.get(), data),
                        "Node %s violates unique constraint. Stored node %s already contains the same value combination of leafs: %s",
                        data.getIdentifier(), indexData.isPresent() ? indexData.get().getIdentifier() : "",
                        uniqueLeafIdentifiers);

        mapKeyToindexKey.put(data.getIdentifier(), indexKey);
        indexMap.put(indexKey, data);
    }

    private void updateAll(final MapNode data) {
        Preconditions.checkState(!sealed);
        for (final MapEntryNode mapEntry : data.getValue()) {
            update(mapEntry);
        }
    }

    @Override
    public Optional<MapEntryNode> get(final IndexKey<?> indexKey) {
        return Optional.fromNullable(indexMap.get(indexKey));
    }

    void remove(final PathArgument id) {
        Preconditions.checkState(!sealed);
        Preconditions.checkArgument(id instanceof NodeIdentifierWithPredicates,
                "Id must be instance of NodeIdentifierWithPredicates.");
        addRemoveOperation(mapKeyToindexKey.get(id));
    }

    @Override
    public void remove(final UniqueIndexKey indexKey) {
        Preconditions.checkState(!sealed);
        Preconditions.checkNotNull(indexKey);
        addRemoveOperation(indexKey);
    }

    private void addRemoveOperation(final UniqueIndexKey uniqueIndexKey) {
        indexChanges.add(new Operation(OperationType.REMOVE, uniqueIndexKey, null));
    }

    private void removeFromIndex(final UniqueIndexKey indexKey) {
        final MapEntryNode removedEntry = indexMap.remove(indexKey);
        if (removedEntry != null) {
            mapKeyToindexKey.remove(removedEntry.getIdentifier());
        }
    }

    @Override
    public UniqueIndex seal() {
        Preconditions.checkState(!sealed);
        applyOperations();
        this.sealed = true;
        return new UniqueIndex(indexMap, mapKeyToindexKey, uniqueLeafIdentifiers);
    }

    private void applyOperations() {
        removeAllOverwrittenEntriesFromIndex();
        for (final Operation operation : indexChanges) {
            switch (operation.getOperationType()) {
            case REMOVE:
                removeFromIndex(operation.getIndexKey());
                break;
            case PUT:
                putToIndex(operation.getIndexKey(), operation.getData());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation over unique index.");
            }
        }
    }

    private void removeAllOverwrittenEntriesFromIndex() {
        for (final Operation operation : indexChanges) {
            switch (operation.getOperationType()) {
            case PUT:
                final UniqueIndexKey originalEntryIndexKey = mapKeyToindexKey.get(operation.getData().getIdentifier());
                if (originalEntryIndexKey != null) {
                    removeFromIndex(originalEntryIndexKey);
                }
                break;
            default:
                break;
            }
        }
    }

    @Override
    public MutableTreeNodeIndex<?, ?> mutable() {
        return this;
    }

    private class Operation {
        private final OperationType operationType;
        private final UniqueIndexKey indexKey;
        private final MapEntryNode data;

        Operation(final OperationType operationType, final UniqueIndexKey indexKey, final MapEntryNode data) {
            this.operationType = operationType;
            this.indexKey = indexKey;
            this.data = data;
        }

        OperationType getOperationType() {
            return operationType;
        }

        UniqueIndexKey getIndexKey() {
            return indexKey;
        }

        MapEntryNode getData() {
            return data;
        }
    }

    private enum OperationType {
        REMOVE, PUT
    }
}
