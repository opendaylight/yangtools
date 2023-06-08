/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link NormalizedContainer} which preserves user supplied ordering and allows addressing of child elements by
 * position. All implementations of this interface must also implement {@link OrderingAware.User}.
 *
 * @param <V> child type
 */
public sealed interface OrderedContainer<V extends NormalizedNode> extends NormalizedContainer<V>, OrderingAware.User
        permits OrderedNodeContainer {
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
