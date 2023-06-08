/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A piece of {@link NormalizedData} which holds some child {@link NormalizedNode}s. This interface should not be used
 * directly, but rather through its specializations.
 *
 * @param <V> Child Node type
 */
public sealed interface NormalizedContainer<V extends NormalizedNode> extends NormalizedData, OrderingAware
        permits NormalizedNodeContainer, DistinctContainer {
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
