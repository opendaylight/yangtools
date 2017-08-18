/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A container node which may need further materialization. Materialized nodes are tracked in a map, which is consulted
 * before creating a new child node from backing data. Since the child is immutable, we do not have to track it.
 */
final class LazyMutableContainerNode extends AbstractMutableContainerNode {
    LazyMutableContainerNode(final AbstractContainerNode parent) {
        this(parent, MapAdaptor.getDefaultInstance().initialSnapshot(1));
    }

    LazyMutableContainerNode(final AbstractContainerNode parent, final Map<PathArgument, TreeNode> children) {
        super(parent, children);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument childId) {
        final TreeNode modified = getModifiedChild(childId);
        if (modified != null) {
            return Optional.of(modified);
        }

        return Optional.ofNullable(AbstractContainerNode.getChildFromData(getData(), childId, getVersion()));
    }
}
