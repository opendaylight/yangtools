/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class UnmaterializedContainerNode extends AbstractContainerNode {
    protected UnmaterializedContainerNode(final NormalizedNode<?, ?> data, final Version version) {
        super(data, version, version);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument child) {
        return getChildFromData(child);
    }

    @Override
    public MutableTreeNode mutable() {
        return new LazyMutableContainerNode(this);
    }
}
