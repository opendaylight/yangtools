/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link NormalizedContainer} which contains distinctly-addressable children.
 *
 * @param <K> Child path argument type
 * @param <V> Child Node type
 */
public sealed interface DistinctContainer<K extends PathArgument, V extends NormalizedNode>
        extends NormalizedContainer<V> permits DistinctNodeContainer, DataContainer {
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
     * @throws NullPointerException if {@code key} is {@code null}
     */
    @Nullable V childByArg(K key);

    /**
     * Attempts to find a child node identified by provided key.
     *
     * @param key Path argument identifying child node
     * @return Optional with child node if child exists. {@link Optional#empty()} if child does not exist
     * @throws NullPointerException if {@code key} is {@code null}
     */
    default Optional<V> findChildByArg(final K key) {
        return Optional.ofNullable(childByArg(key));
    }

    /**
     * Returns a child node identified by provided key, asserting its presence.
     *
     * @param key Path argument identifying child node
     * @return Matching child node
     * @throws NullPointerException if {@code key} is {@code null}
     * @throws VerifyException if the child does not exist
     */
    default @NonNull V getChildByArg(final K key) {
        return verifyNotNull(childByArg(key), "No child matching %s", key);
    }

    /**
     * Return a {@link Map} view of this node. Note that the iteration order of the returned is map is not defined in
     * this interface.
     *
     * @return Map view of this node.
     */
    @NonNull Map<K, V> asMap();
}
