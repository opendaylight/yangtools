/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A cursor holding a logical position within a {@link DataTreeSnapshot}. It allows operations relative to that
 * position, as well as moving the position up or down the tree. Implementations are expected to be NOT thread-safe.
 */
public interface DataTreeSnapshotCursor extends AutoCloseable {
    /**
     * Move the cursor to the specified child of the current position.
     *
     * @param child Child identifier
     * @throws BackendFailedException when an implementation-specific error occurs
     *                                while servicing the request.
     * @throws IllegalArgumentException when specified identifier does not identify
     *                                  a valid child, or if that child is not an
     *                                  instance of {@link NormalizedNodeContainer}.
     */
    void enter(@NonNull PathArgument child);

    /**
     * Move the cursor to the specified child of the current position. This is
     * the equivalent of multiple invocations of {@link #enter(PathArgument)},
     * except the operation is performed all at once.
     *
     * @param path Nested child identifier
     * @throws BackendFailedException when an implementation-specific error occurs
     *                                while servicing the request.
     * @throws IllegalArgumentException when specified path does not identify
     *                                  a valid child, or if that child is not an
     *                                  instance of {@link NormalizedNodeContainer}.
     */
    default void enter(final @NonNull PathArgument... path) {
        enter(List.of(path));
    }

    /**
     * Move the cursor to the specified child of the current position. This is
     * equivalent to {@link #enter(PathArgument...)}, except it takes an {@link Iterable}
     * argument.
     *
     * @param path Nested child identifier
     * @throws BackendFailedException when an implementation-specific error occurs
     *                                while servicing the request.
     * @throws IllegalArgumentException when specified path does not identify
     *                                  a valid child, or if that child is not an
     *                                  instance of {@link NormalizedNodeContainer}.
     */
    void enter(@NonNull Iterable<PathArgument> path);

    /**
     * Move the cursor up to the parent of current position. This is equivalent of
     * invoking <code>exit(1)</code>.
     *
     * @throws IllegalStateException when exiting would violate containment, typically
     *                               by attempting to exit more levels than previously
     *                               entered.
     */
    void exit();

    /**
     * Move the cursor up by specified amounts of steps from the current position.
     * This is equivalent of invoking {@link #exit()} multiple times, except the
     * operation is performed atomically.
     *
     * @param depth number of steps to exit
     * @throws IllegalArgumentException when depth is negative.
     * @throws IllegalStateException when exiting would violate containment, typically
     *                               by attempting to exit more levels than previously
     *                               entered.
     */
    void exit(int depth);

    /**
     * Read a particular node from the snapshot.
     *
     * @param child Child identifier
     * @return Optional result encapsulating the presence and value of the node
     * @throws BackendFailedException when implementation-specific error occurs while
     *                                servicing the request.
     * @throws IllegalArgumentException when specified path does not identify a valid child.
     */
    @Nullable NormalizedNode readNode(@NonNull PathArgument child);

    /**
     * Close this cursor. Attempting any further operations on the cursor will lead to undefined behavior.
     */
    @Override
    void close();
}
