/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link NormalizedNodeContainer} which preserves user supplied ordering and allows addressing of child elements by
 * position. All implementations of this interface must also implement {@link OrderingAware.User}. This interface should
 * not be implemented directly, but rather implementing one of it's subclasses
 * <ul>
 *   <li>{@link UnkeyedListNode}</li>
 *   <li>{@link UserLeafSetNode}</li>
 *   <li>{@link UserMapNode}</li>
 * </ul>
 *
 * @param <V> child type
 */
// FIXME: 9.0.0: we really want to do a List<@NonNull V> body(), but need to reconcile that with key-based lookup in
//               implementations -- and those are using only a Map internally.
public sealed interface OrderedNodeContainer<V extends NormalizedNode>
        extends NormalizedNodeContainer<V>, MixinNode, OrderingAware.User
        permits UnkeyedListNode, UserLeafSetNode, UserMapNode {
    @Override
    NodeIdentifier getIdentifier();

    /**
     * Returns child node by position.
     *
     * @param position Position of child node
     * @return Child Node
     * @throws IndexOutOfBoundsException Out of bound Exception
     */
    @NonNull V childAt(int position);

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
