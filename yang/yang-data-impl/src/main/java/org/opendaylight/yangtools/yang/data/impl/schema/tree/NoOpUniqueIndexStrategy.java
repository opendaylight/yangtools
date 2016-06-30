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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;

final class NoOpUniqueIndexStrategy extends UniqueIndexStrategyBase {
    static final NoOpUniqueIndexStrategy INSTANCE = new NoOpUniqueIndexStrategy();

    private NoOpUniqueIndexStrategy() {
        // Hidden on purpose
    }

    @Override
    public Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> createIndexesFromData(
            final NormalizedNode<?, ?> newValue) {
        return ImmutableMap.of();
    }

    @Override
    public void updateIndexes(final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes,
            final NormalizedNode<?, ?> data) {
        // No Op.
    }

    @Override
    public void removeFromIndexes(final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes,
            final PathArgument id) {
        // No Op.
    }

    @Override
    protected List<Set<YangInstanceIdentifier>> getUniqueConstraintsLeafIds() {
        return ImmutableList.of();
    }
}