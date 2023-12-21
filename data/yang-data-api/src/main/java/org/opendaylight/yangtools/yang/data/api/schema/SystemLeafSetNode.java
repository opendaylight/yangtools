/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;

/**
 * {@link LeafSetNode} which does not preserve user-supplied ordering. This node represents a data instance of
 * a {@code leaf-list} without a {@code ordered-by user;} substatement, i.e. when the {@code leaf-list} is effectively
 * {@code ordered-by system;}.
 *
 * @param <T> Value type of Leaf entries
 */
// FIXME: 9.0.0: we really want to do a Set<@NonNull V> body(), but need to reconcile that with key-based lookup in
//               implementations -- and those are using only a Map internally.
public non-sealed interface SystemLeafSetNode<T> extends LeafSetNode<T>, OrderingAware.System {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<SystemLeafSetNode> contract() {
        return SystemLeafSetNode.class;
    }

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    /**
     * A builder of {@link SystemLeafSetNode}s.
     */
    interface Builder<T> extends ListNodeBuilder<T, SystemLeafSetNode<T>> {
        // Just a specialization
    }
}
