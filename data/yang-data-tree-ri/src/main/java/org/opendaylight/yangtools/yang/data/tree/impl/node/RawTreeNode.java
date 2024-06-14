/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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
 * Abstract base class for undecorated tree nodes.
 */
@NonNullByDefault
public abstract sealed class RawTreeNode extends TreeNode permits AbstractContainerNode, ValueNode {
    private final NormalizedNode data;
    private final Version version;

    RawTreeNode(final NormalizedNode data, final Version version) {
        this.data = requireNonNull(data);
        this.version = requireNonNull(version);
    }

    @Override
    public final NormalizedNode data() {
        return data;
    }

    @Override
    public final Version version() {
        return version;
    }

    /**
     * Get a mutable, isolated copy of the node.
     *
     * @return Mutable copy
     */
    public abstract MutableTreeNode toMutable();

}
