/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A decorated {@link TreeNode}: it has a backing base and decorates it somehow.
 */
@NonNullByDefault
public abstract non-sealed class DecoratingTreeNode extends TreeNode {
    protected final TreeNode base;

    protected DecoratingTreeNode(final TreeNode base) {
        this.base = requireNonNull(base);
    }

    @Override
    public final NormalizedNode data() {
        return base.data();
    }

    @Override
    public final Version version() {
        return base.version();
    }

    @Override
    public final Version subtreeVersion() {
        return base.subtreeVersion();
    }
}
