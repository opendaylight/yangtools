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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UniqueIndex implements TreeNodeIndex{
    private static final Logger LOG = LoggerFactory.getLogger(UniqueIndex.class);

    private final Map<List<Object>, MapEntryNode> indexMap;
    private final List<YangInstanceIdentifier> uniqueLeafIdentifiers;

    UniqueIndex(final List<YangInstanceIdentifier> uniqueNodesIdentifiers) {
        this.uniqueLeafIdentifiers = Preconditions.checkNotNull(uniqueNodesIdentifiers);
        this.indexMap = new HashMap<>();
    }

    UniqueIndex(final UniqueConstraint uniqueConstraint) {
        final List<YangInstanceIdentifier> uniqueLeafIdentifiers = new ArrayList<>();
        final Collection<Relative> tag = uniqueConstraint.getTag();
        for (final Relative relative : tag) {
            final YangInstanceIdentifier id = createYangInstanceIdentifier(relative);
            uniqueLeafIdentifiers.add(id);
        }

        this.uniqueLeafIdentifiers = ImmutableList.copyOf(uniqueLeafIdentifiers);
        this.indexMap = new HashMap<>();
    }

    private YangInstanceIdentifier createYangInstanceIdentifier(final Relative relative) {
        YangInstanceIdentifier id = YangInstanceIdentifier.EMPTY;
        // :FIXME find out how to concert this safely
        for (final QName qname : relative.getPathFromRoot()) {
            id = id.node(qname);
        }
        return id;
    }

    void ifApplicableCheckIndex(final TreeNode tree) {
        ifApplicableCheckIndex(tree.getData());
    }

    void ifApplicableCheckIndex(final NormalizedNode<?, ?> data) {
        Preconditions.checkArgument(data instanceof MapEntryNode);

        final List<Object> uniqueIndexKey = new ArrayList<>();
        for (final YangInstanceIdentifier id : uniqueLeafIdentifiers) {
            final Optional<NormalizedNode<?, ?>> findNode = NormalizedNodes.findNode(data, id);
            if (!findNode.isPresent() || findNode.get().getValue() == null) {
                // TODO: Log the index is not applicable\
                LOG.debug("Node {} is missing. Index check is skipped.", id);
                return;
            } else {
                uniqueIndexKey.add(findNode.get().getValue());
            }
        }

        checkIndex(uniqueIndexKey, (MapEntryNode) data);
    }

    private void checkIndex(final List<Object> uniqueIndexKey, final MapEntryNode data) {
        final MapEntryNode storedMapEntryNode = indexMap.get(uniqueIndexKey);
        Preconditions.checkArgument(storedMapEntryNode == null,
                "Node %s violates unique constraint. Stored node %s already contains the same value combination of leafs: %s",
                data.getIdentifier(), storedMapEntryNode, uniqueLeafIdentifiers);
    }

    List<YangInstanceIdentifier> getUniqueLeafIdentifiers() {
        return uniqueLeafIdentifiers;
    }
}
