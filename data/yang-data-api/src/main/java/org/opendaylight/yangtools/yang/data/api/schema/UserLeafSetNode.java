/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;

/**
 * {@link LeafSetNode} which preserves user-supplied ordering. This node represents a data instance of
 * a {@code leaf-list} with a {@code ordered-by user;} substatement.
 *
 * @param <T> Value type of Leaf entries
 */
public non-sealed interface UserLeafSetNode<T> extends LeafSetNode<T>, OrderedNodeContainer<LeafSetEntryNode<T>> {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<UserLeafSetNode> contract() {
        return UserLeafSetNode.class;
    }

    /**
     * A builder of {@link UserLeafSetNode}s.
     */
    interface Builder<T> extends ListNodeBuilder<T, UserLeafSetNode<T>> {
        /**
         * Return the resulting {@link UserLeafSetNode}.
         *
         * @return resulting {@link UserLeafSetNode}
         * @throws IllegalStateException if this builder does not have sufficient state
         */
        @NonNull UserLeafSetNode<T> build();
    }
}
