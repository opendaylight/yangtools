/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.io.Externalizable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

@Beta
public interface LinearPath<P extends LinearPath<P, C>, C extends Comparable<C> & Immutable>
    extends Immutable, Path<P> {
    /**
     * Check if this path is empty.
     *
     * @return True if this path is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Return an optimized version of this path, useful when the path object will be used very frequently.
     *
     * @return A optimized equivalent instance.
     */
    @NonNull P toOptimized();

    @NonNull Externalizable toExternalizable();

    /**
     * Return the conceptual parent {@link LinearPath}, which has one item less in {@link #getPathFromRoot()}.
     *
     * @return Parent {@link LinearPath}, or null if this object is empty.
     */
    @Nullable P getParent();

    /**
     * Returns the last PathArgument. This is equivalent of iterating to the last element of the iterable returned
     * by {@link #getPathFromRoot()}, or the first element returned from {@link #getPathTowardsRoot()}.
     *
     * @return The last path component, or null if there are no components.
     */
    @Nullable C getLastComponent();

    /**
     * Returns an ordered iteration of path arguments.
     *
     * @return Immutable iteration of path arguments.
     */
    @NonNull List<C> getPathFromRoot();

    /**
     * Returns an iterable of path arguments in reverse order. This is useful
     * when walking up a tree organized this way.
     *
     * @return Immutable iterable of path arguments in reverse order.
     */
    @NonNull List<C> getPathTowardsRoot();

    @Override
    default boolean contains(final @NonNull P other) {
        if (this == other) {
            return true;
        }

        checkArgument(other != null, "other should not be null");
        final Iterator<C> lit = getPathFromRoot().iterator();
        final Iterator<C> oit = other.getPathFromRoot().iterator();

        while (lit.hasNext()) {
            if (!oit.hasNext()) {
                return false;
            }

            if (!lit.next().equals(oit.next())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return the ancestor path with a particular depth, e.g. number of path components.
     *
     * @param depth Ancestor depth
     * @return Ancestor path
     * @throws IllegalArgumentException if the specified depth is negative or is greater than the depth of this object.
     */
    @Nonnull P getAncestor(int depth);

    /**
     * Get the relative path from an ancestor. This method attempts to perform the reverse of concatenating a base
     * (ancestor) and a path.
     *
     * @param ancestor Ancestor against which the relative path should be calculated
     * @return This object's relative path from parent, or Optional.absent() if the specified parent is not in fact an
     *         ancestor of this object.
     * @throws NullPointerException if {@code ancestor} is null
     */
    Optional<P> relativeTo(P ancestor);
}
