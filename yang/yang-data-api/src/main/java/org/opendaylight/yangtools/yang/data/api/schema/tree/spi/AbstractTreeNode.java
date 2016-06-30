/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A very basic data tree node. Contains some versioned data.
 */
abstract class AbstractTreeNode implements TreeNode {
    private final NormalizedNode<?, ?> data;
    private final Version version;
    private final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes;

    protected AbstractTreeNode(final NormalizedNode<?, ?> data, final Version version) {
        this(data, version, ImmutableMap.of());
    }

    protected AbstractTreeNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes) {
        this.data = Preconditions.checkNotNull(data);
        this.version = Preconditions.checkNotNull(version);
        this.indexes = MapAdaptor.getDefaultInstance().optimize(Preconditions.checkNotNull(indexes));
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    @Override
    public final Version getVersion() {
        return version;
    }

    @Override
    public final NormalizedNode<?, ?> getData() {
        return data;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("version", version)).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> getIndexes() {
        return indexes;
    }

    @Override
    public Optional<? extends NormalizedNode<?, ?>> getFromIndex(final IndexKey<?> indexKey) {
        final Set<?> identifierSet = indexKey.getValue().keySet();
        return indexes.get(identifierSet).get(indexKey);
    }
}
