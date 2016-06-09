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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UniqueIndex {
    private static final Logger LOG = LoggerFactory.getLogger(UniqueIndex.class);

    private final Map<List<Object>, MapEntryNode> storedValues;
    private Map<List<Object>, MapEntryNode> modificationValues;
    private final List<YangInstanceIdentifier> uniqueLeafIdentifiers;

    UniqueIndex(final List<YangInstanceIdentifier> uniqueNodesIdentifiers) {
        this.uniqueLeafIdentifiers = Preconditions.checkNotNull(uniqueNodesIdentifiers);
        this.storedValues = new HashMap<>();
        this.modificationValues = new HashMap<>();
    }

    UniqueIndex(final UniqueConstraint uniqueConstraint) {

    }

    void ifApplicableCheckIndex(final TreeNode tree) {
        final NormalizedNode<?, ?> data = tree.getData();
        Preconditions.checkArgument(data instanceof MapEntryNode);

        final List<Object> uniqueIndexKey = new ArrayList<>();
        for (final YangInstanceIdentifier id : uniqueLeafIdentifiers) {
            final Optional<NormalizedNode<?, ?>> findNode = NormalizedNodes.findNode(data, id);
            if (!findNode.isPresent() || findNode.get().getValue() == null) {
                // TODO: Log the index is not applicable\
                LOG.debug("...");
                return;
            } else {
                uniqueIndexKey.add(findNode.get().getValue());
            }
        }

        checkIndex(uniqueIndexKey, (MapEntryNode) data);
    }

    private void checkIndex(final List<Object> uniqueIndexKey, final MapEntryNode data) {
        final MapEntryNode storedMapEntryNode = storedValues.get(uniqueIndexKey);
        Preconditions.checkArgument(storedMapEntryNode == null,
                "Node %s violates unique constraint. Node %s already contains the same value combination of leafs: %s",
                data.getIdentifier(), storedMapEntryNode.getIdentifier(), uniqueLeafIdentifiers);

        final MapEntryNode modificationMapEntryNode = modificationValues.put(uniqueIndexKey, data);
        Preconditions.checkArgument(modificationMapEntryNode == null,
                "Node %s violates unique constraint. Node %s already contains the same value combination of leafs: %s",
                data.getIdentifier(), modificationMapEntryNode.getIdentifier(), uniqueLeafIdentifiers);
    }

    void commit() {
        storedValues.putAll(modificationValues);
        modificationValues = new HashMap<>();
    }

    List<YangInstanceIdentifier> getUniqueLeafIdentifiers() {
        return uniqueLeafIdentifiers;
    }
}
