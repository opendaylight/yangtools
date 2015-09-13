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
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A fully-modified node. It is the same thing as {@link LazyContainerNode}, except we know we have all children
 * in the map, so we skip lookups in the data.
 */
final class MaterializedContainerNode extends AbstractModifiedContainerNode {
    private final Map<PathArgument, TreeNode> children;

    protected MaterializedContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion) {
        super(data, version, subtreeVersion);
        this.children = Preconditions.checkNotNull(children);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument key) {
        return Optional.fromNullable(children.get(key));
    }

    @Override
    public MutableTreeNode mutable() {
        return new MaterializedMutableContainerNode(this, MapAdaptor.getDefaultInstance().takeSnapshot(children));
    }
}
