/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Collections2;
import java.util.Map;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Lazily-materialized container node. Any new/modified children are tracked in a map. This map is consulted before
 * instantiating a child node from data node. Resulting node is not cached.
 */
final class LazyContainerNode extends AbstractModifiedContainerNode {
    LazyContainerNode(final NormalizedNode data, final Version version, final Version subtreeVersion) {
        this(data, version, MapAdaptor.getDefaultInstance().initialSnapshot(1), subtreeVersion);
    }

    LazyContainerNode(final NormalizedNode data, final Version version,
            final Map<PathArgument, TreeNode> children, final Version subtreeVersion) {
        super(data, version, children, subtreeVersion);
    }

    @Override
    public MutableTreeNode toMutable(final Version nextSubtreeVersion) {
        final var snapshot = snapshotChildren();
        return snapshot.size() == castData().size()
            ? new MaterializedMutableContainerNode(this, nextSubtreeVersion, snapshot)
            : new LazyMutableContainerNode(this, nextSubtreeVersion, snapshot);
    }

    @Override
    public TreeNode childByArg(final PathArgument arg) {
        final TreeNode modified;
        return (modified = getModifiedChild(arg)) == null ? childFromData(arg) : modified;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        // Modified children add added by superclass. Here we filter the other children.
        return super.addToStringAttributes(helper).add("untouched", Collections2.filter(castData().body(),
            input -> getModifiedChild(input.name()) == null));
    }
}
