/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link NormalizedNodeContainer} which preserves user supplied ordering and allows addressing of child elements by
 * position. All implementations of this interface must also implement {@link OrderingAware.User}.
 *
 * @param <V> child type
 */
public interface OrderedNodeContainer<V extends NormalizedNode>
        extends NormalizedNodeContainer<V>, MixinNode, OrderingAware.User {
    @Override
    NodeIdentifier getIdentifier();

    @Override
    List<@NonNull V> body();

    /**
     * Returns child node by position.
     *
     * @param position Position of child node
     * @return Child Node
     * @throws IndexOutOfBoundsException Out of bound Exception
     */
    default @NonNull V childAt(final int position) {
        return body().get(position);
    }

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
