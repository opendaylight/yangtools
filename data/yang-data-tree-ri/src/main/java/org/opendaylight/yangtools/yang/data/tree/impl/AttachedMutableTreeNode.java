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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

abstract class AttachedMutableTreeNode implements MutableTreeNode {
    private final MutableTreeNode delegate;

    AttachedMutableTreeNode(final MutableTreeNode delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final void setData(final NormalizedNode data) {
        delegate.setData(data);
    }

    @Override
    public final void setSubtreeVersion(final Version subtreeVersion) {
        delegate.setSubtreeVersion(subtreeVersion);
    }

    @Override
    public TreeNode putChild(final TreeNode child) {
        return delegate.putChild(child);
    }

    @Override
    public TreeNode removeChild(final PathArgument id) {
        return delegate.removeChild(id);
    }

    @Override
    public final TreeNode childByArg(final PathArgument arg) {
        final TreeNode child = delegate.childByArg(arg);
        return child == null ? null : wrapChild(child);
    }

    @NonNull TreeNode wrapChild(final @NonNull TreeNode child) {
        return child;
    }

    @Override
    public abstract TreeNode seal();

    final TreeNode sealDelegate() {
        return delegate.seal();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}
