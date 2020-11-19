/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A TreeNode capable of holding child nodes. The fact that any of the children
 * changed is tracked by the subtree version.
 */
abstract class AbstractContainerNode extends AbstractTreeNode {
    private final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes;

    AbstractContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> indexes) {
        super(data, version);
        this.indexes = MapAdaptor.getDefaultInstance().optimize(Preconditions.checkNotNull(indexes));
    }

    @SuppressWarnings("unchecked")
    protected final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> castData() {
        return (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) getData();
    }

    protected final Optional<TreeNode> getChildFromData(final PathArgument childId) {
        // We do not cache the instantiated node as it is dirt cheap
        return Optional.fromNullable(getChildFromData(castData(), childId, getVersion()));
    }

    @Override
    public Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> getIndexes() {
        return indexes;
    }

    @Override
    public Optional<? extends NormalizedNode<?, ?>> getFromIndex(final IndexKey<?> indexKey) {
        final Set<?> identifierSet = indexKey.getValue().keySet();
        return indexes.get(identifierSet).get(indexKey);
    }

    static TreeNode getChildFromData(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data,
            final PathArgument childId, final Version version) {
        final Optional<NormalizedNode<?, ?>> child = data.getChild(childId);
        return child.isPresent() ? TreeNodeFactory.createTreeNode(child.get(), version) : null;
    }
}
