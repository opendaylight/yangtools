/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * At-rest view of an indexed unique child. It defers to its delegate for everything, but also holds an associated
 * unique value vector.
 */
final class UniqueVectorTreeNode extends AttachedTreeNode {
    private final Object vector;

    UniqueVectorTreeNode(final TreeNode delegate, final Object vector) {
        super(delegate);
        this.vector = vector;
    }

    Object vector() {
        return vector;
    }
}
