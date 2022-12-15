/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Node which is not leaf, but has child {@link NormalizedNode}s as its value. It provides iteration over its child
 * nodes via {@link #body()}. More convenient access to child nodes are provided by {@link DistinctNodeContainer} and
 * {@link OrderedNodeContainer}.
 *
 * @param <V> Child Node type
 */
public sealed interface NormalizedNodeContainer<V extends NormalizedNode> extends NormalizedData, OrderingAware
        permits DistinctNodeContainer, OrderedNodeContainer {
    /**
     * {@inheritDoc}
     *
     * <p>
     * Returns iteration of all child nodes. Order of returned child nodes may be defined by subinterfaces.
     */
    @Override
    Collection<@NonNull V> body();

    /**
     * Return the logical size of this container body. The default implementation defers to {@code body().size()}.
     *
     * @return Size of this container's body.
     */
    default int size() {
        return body().size();
    }

    /**
     * Determine whether this container body is empty. The default implementation defers to {@code body().isEmpty()}.
     *
     * @return True if this container has an empty body.
     */
    default boolean isEmpty() {
        return body().isEmpty();
    }
}
