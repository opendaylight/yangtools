/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A fully-modified node -- we know we have all children, so it performs lookups only.
 */
final class MaterializedContainerNode extends AbstractModifiedContainerNode {
    protected MaterializedContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion) {
        super(data, version, children, subtreeVersion);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument childId) {
        return Optional.ofNullable(getModifiedChild(childId));
    }

    @Override
    public MutableTreeNode mutable() {
        return new MaterializedMutableContainerNode(this, snapshotChildren());
    }
}
