/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link NormalizedNodeContainer} which contains directly-addressable children. It
 *
 * <p>
 * NormalizedNodeContainer does not have a value, but it has a child nodes. Definition of possible and valid child nodes
 * is introduced in subclasses of this interface.
 *
 * <p>
 * This interface should not be used directly, but rather use of of derived subclasses such as
 * {@link DataContainerNode}, {@link MapNode}, {@link LeafSetNode}.
 *
 * @param <I> Path argument type
 * @param <K> Child path argument type
 * @param <V> Child Node type
 */
public interface DistinctNodeContainer<I extends PathArgument, K extends PathArgument, V extends NormalizedNode>
        extends NormalizedNodeContainer<I, V> {
    /**
     * {@inheritDoc}
     *
     * <p>
     * All nodes returned in this iterable, MUST also be accessible via {@link #childByArg(PathArgument)} using their
     * associated identifier.
     *
     * @return Iteration of all child nodes
     */
    @Override
    Collection<@NonNull V> body();

    /**
     * Returns a child node identified by provided key.
     *
     * @param key Path argument identifying child node
     * @return Matching child node, or null if no matching child exists
     * @throws NullPointerException if {@code key} is null
     */
    @Nullable V childByArg(K key);

    /**
     * Attempts to find a child node identified by provided key.
     *
     * @param key Path argument identifying child node
     * @return Optional with child node if child exists. {@link Optional#empty()} if child does not exist
     * @throws NullPointerException if {@code key} is null
     */
    default Optional<V> findChildByArg(final K key) {
        return Optional.ofNullable(childByArg(key));
    }
}
