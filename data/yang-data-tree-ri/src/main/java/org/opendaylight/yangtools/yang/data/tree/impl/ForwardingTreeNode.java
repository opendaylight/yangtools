/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

abstract class ForwardingTreeNode implements TreeNode {
    private final TreeNode delegate;

    ForwardingTreeNode(final TreeNode delegate) {
        delegate = requireNonNull(delegate);
    }

    @Override
    public final PathArgument getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public final Version getVersion() {
        return delegate.getVersion();
    }

    @Override
    public final Version getSubtreeVersion() {
        return delegate.getSubtreeVersion();
    }

    @Override
    public final NormalizedNode getData() {
        return delegate.getData();
    }

    @Override
    public abstract MutableTreeNode mutable();

    final TreeNode delegate() {
        return delegate;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}
