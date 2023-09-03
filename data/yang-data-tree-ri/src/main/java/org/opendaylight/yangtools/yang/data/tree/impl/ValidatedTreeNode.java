/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

@SuppressWarnings("null")
record ValidatedTreeNode(@Nullable TreeNode treeNode) {
    ValidatedTreeNode(final Optional<? extends TreeNode> optional) {
        this(optional.orElse(null));
    }

    @NonNull Optional<TreeNode> toOptional() {
        return Optional.ofNullable(treeNode);
    }
}
