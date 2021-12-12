/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A container node which has not seen a modification. All nodes underneath it share the same subtree version.
 */
final class SimpleContainerNode extends AbstractContainerNode {
    protected SimpleContainerNode(final NormalizedNode data, final Version version) {
        super(data, version);
    }

    @Override
    public Version getSubtreeVersion() {
        return getVersion();
    }

    @Override
    public TreeNode childByArg(final PathArgument arg) {
        return getChildFromData(arg);
    }

    @Override
    public MutableTreeNode mutable() {
        return new LazyMutableContainerNode(this);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("data", getData());
    }
}
