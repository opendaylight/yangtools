/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

final class UniqueIndexTreeNode extends AttachedTreeNode {
    private final @Nullable Map<UniqueValidator<?>, Map<Object, TreeNode>> index;

    UniqueIndexTreeNode(final TreeNode delegate) {
        super(delegate);
        index = null;
    }

    UniqueIndexTreeNode(final TreeNode delegate, final Map<UniqueValidator<?>, Map<Object, TreeNode>> vectorToChild) {
        super(delegate);
        index = requireNonNull(vectorToChild);
    }

    @Nullable Map<UniqueValidator<?>, Map<Object, TreeNode>> index() {
        return index;
    }
}
