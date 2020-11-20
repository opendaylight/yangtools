/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

abstract class ForwardingMutableTreeNode implements MutableTreeNode {
    private final MutableTreeNode delegate;

    ForwardingMutableTreeNode(final MutableTreeNode delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void setData(final NormalizedNode<?, ?> data) {
        delegate.setData(data);
    }

    @Override
    public void addChild(final TreeNode child) {
        delegate.addChild(child);
    }

    @Override
    public void removeChild(final PathArgument id) {
        delegate.removeChild(id);
    }

    @Override
    public final Optional<? extends TreeNode> getChild(final PathArgument child) {
        return delegate.getChild(child);
    }

    @Override
    public final void setSubtreeVersion(final Version subtreeVersion) {
        delegate.setSubtreeVersion(subtreeVersion);
    }

    @Override
    public abstract @NonNull TreeNode seal();

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
