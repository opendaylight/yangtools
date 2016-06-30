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
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexKey;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;

final class UniqueIndex implements TreeNodeIndex<UniqueIndexKey, MapEntryNode> {
    private final Map<UniqueIndexKey, MapEntryNode> indexMap;
    private final Map<NodeIdentifierWithPredicates, UniqueIndexKey> mapKeyToindexKey;
    private final Set<YangInstanceIdentifier> uniqueLeafIdentifiers;

    UniqueIndex(final Map<UniqueIndexKey, MapEntryNode> indexMap,
            final Map<NodeIdentifierWithPredicates, UniqueIndexKey> mapKeyToindexKey,
            final Set<YangInstanceIdentifier> uniqueLeafIdentifiers) {
        Preconditions.checkNotNull(indexMap);
        Preconditions.checkNotNull(mapKeyToindexKey);
        Preconditions.checkNotNull(uniqueLeafIdentifiers);

        this.indexMap = MapAdaptor.getDefaultInstance().optimize(indexMap);
        this.mapKeyToindexKey = MapAdaptor.getDefaultInstance().optimize(mapKeyToindexKey);
        this.uniqueLeafIdentifiers = ImmutableSet.copyOf(uniqueLeafIdentifiers);
    }

    @Override
    public Optional<MapEntryNode> get(final IndexKey<?> indexKey) {
        return Optional.fromNullable(indexMap.get(indexKey));
    }

    public Set<YangInstanceIdentifier> getUniqueLeafIdentifiers() {
        return uniqueLeafIdentifiers;
    }

    public Optional<UniqueIndexKey> get(final NodeIdentifierWithPredicates mapKey) {
        return Optional.fromNullable(mapKeyToindexKey.get(mapKey));
    }

    @Override
    public MutableUniqueIndex mutable() {
        return new MutableUniqueIndex(indexMap, mapKeyToindexKey, uniqueLeafIdentifiers);
    }
}
