/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A capture of a tree-like construct, which can be formatted into a pretty-printed tree. The string can be acquired
 * via {@link #get()}.
 *
 * <p>
 * This concept is purposefully designed as an abstract class which defers its {@link #toString()} to {@link #get()}, as
 * it allows convenient and light-weight use with logging:
 *
 * <pre>
 *   <code>
 *     PrettyTreeAware treeLike;
 *     LOG.debug("Tree is {}", treeLike.prettyTree());
 *   </code>
 * </pre>
 */
public abstract class PrettyTree implements Supplier<String> {
    @Override
    public @NonNull String get() {
        final var sb = new StringBuilder();
        appendTo(sb, 0);
        return sb.toString();
    }

    @Override
    public final @NonNull String toString() {
        return get();
    }

    /**
     * Format this object into specified {@link StringBuilder} starting at specified initial depth.
     *
     * @param sb Target {@link StringBuilder}
     * @param depth Initial nesting depth
     * @throws NullPointerException if {@code sb} is null
     * @throws IllegalArgumentException if {@code depth} is negative
     */
    public abstract void appendTo(StringBuilder sb, int depth);

    /**
     * Append a number of spaces equivalent to specified tree nesting depth into the specified {@link StringBuilder}.
     *
     * @param sb Target {@link StringBuilder}
     * @param depth Nesting depth
     * @throws NullPointerException if {@code sb} is null
     * @throws IllegalArgumentException if {@code depth} is negative
     */
    protected static final void appendIndent(final StringBuilder sb, final int depth) {
        checkArgument(depth >= 0, "Invalid depth %s", depth);
        PrettyTreeIndent.indent(sb, depth);
    }
}
