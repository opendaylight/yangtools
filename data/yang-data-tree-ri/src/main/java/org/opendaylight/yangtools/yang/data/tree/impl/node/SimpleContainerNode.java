/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A container node which has not seen a modification. All nodes underneath it share the same subtree version.
 */
@NonNullByDefault
final class SimpleContainerNode extends AbstractContainerNode {
    SimpleContainerNode(final NormalizedNode data, final Version version) {
        super(data, version);
    }

    @Override
    public Version subtreeVersion() {
        return incarnation();
    }

    @Override
    public @Nullable TreeNode childByArg(final PathArgument arg) {
        return childFromData(arg);
    }

    @Override
    public MutableTreeNode toMutable(final Version nextSubtreeVersion) {
        return new LazyMutableContainerNode(this, nextSubtreeVersion);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("data", data());
    }
}
