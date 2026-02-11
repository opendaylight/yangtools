/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A {@link TreeNode} which decorates another {@link TreeNode}.
 */
@NonNullByDefault
abstract sealed class DecoratingTreeNode extends TreeNode permits MapUniqueTreeNode {
    final TreeNode delegate;

    DecoratingTreeNode(final TreeNode delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final NormalizedNode data() {
        return delegate.data();
    }

    @Override
    public final Version incarnation() {
        return delegate.incarnation();
    }

    @Override
    public final Version subtreeVersion() {
        return delegate.subtreeVersion();
    }

    @Override
    public final @Nullable TreeNode childByArg(final PathArgument arg) {
        return delegate.childByArg(arg);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}

