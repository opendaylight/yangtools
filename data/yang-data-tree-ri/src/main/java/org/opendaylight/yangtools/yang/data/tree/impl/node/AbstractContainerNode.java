/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A TreeNode capable of holding child nodes. The fact that any of the children
 * changed is tracked by the subtree version.
 */
@NonNullByDefault
abstract class AbstractContainerNode extends TreeNode {
    AbstractContainerNode(final NormalizedNode data, final Version incarnation) {
        super(data, incarnation);
    }

    @SuppressWarnings("unchecked")
    final DistinctNodeContainer<PathArgument, NormalizedNode> castData() {
        return (DistinctNodeContainer<PathArgument, NormalizedNode>) data();
    }

    final @Nullable TreeNode childFromData(final PathArgument childId) {
        // We do not cache the instantiated node as it is dirt cheap
        return childFromData(castData(), childId, incarnation());
    }

    static final @Nullable TreeNode childFromData(final DistinctNodeContainer<PathArgument, NormalizedNode> data,
            final PathArgument childId, final Version incarnation) {
        final var child = data.childByArg(childId);
        return child != null ? TreeNode.of(child, incarnation) : null;
    }
}
