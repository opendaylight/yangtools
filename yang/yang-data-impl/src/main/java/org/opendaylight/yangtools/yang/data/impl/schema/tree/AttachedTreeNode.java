/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.VerifyException;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * A {@link TreeNode} which holds additional data and undergoes custom lifecycle. Most motably it does not support
 * {@link #mutable()}, as that transition is expected to be done through an external means.
 */
abstract class AttachedTreeNode extends ForwardingTreeNode {
    AttachedTreeNode(final TreeNode delegate) {
        super(delegate);
    }

    @Override
    public final Optional<? extends TreeNode> getChild(final PathArgument child) {
        return delegate().getChild(child);
    }

    @Override
    public final MutableTreeNode mutable() {
        throw new VerifyException("Attempted to mutate at-rest node " + this);
    }
}
