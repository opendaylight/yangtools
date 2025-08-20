/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A source of text passable for {@link PrettyTree}. It appended to {@link Appendable}.
 */
@Beta
@NonNullByDefault
public interface PrettyString {
    /**
     * Format this object into specified {@link Appendable} starting at specified initial depth.
     *
     * @param appendable target {@link Appendable}
     * @return the {@link Appendable}
     * @throws NullPointerException if {@code appendable} is {@code null}
     * @throws IllegalArgumentException if {@code depth} is negative
     * @throws IOException if an I/O error occurs
     */
    default Appendable appendTo(final Appendable appendable) throws IOException {
        return appendTo(appendable, 0);
    }

    /**
     * Format this object into specified {@link Appendable} starting at specified initial depth.
     *
     * @param appendable Target {@link Appendable}
     * @param depth Initial nesting depth
     * @return the {@link Appendable}
     * @throws NullPointerException if {@code appendable} is {@code null}
     * @throws IllegalArgumentException if {@code depth} is negative
     * @throws IOException if an I/O error occurs
     */
    Appendable appendTo(Appendable appendable, int depth) throws IOException;

    /**
     * Format this object into specified {@link StringBuilder} starting at specified initial depth.
     *
     * @param sb Target {@link StringBuilder}
     * @return the {@link StringBuilder}
     * @throws NullPointerException if {@code sb} is {code null}
     * @throws IllegalArgumentException if {@code depth} is negative
     */
    default StringBuilder appendTo(final StringBuilder sb) {
        return appendTo(sb, 0);
    }

    /**
     * Format this object into specified {@link StringBuilder} starting at specified initial depth.
     *
     * @param sb Target {@link StringBuilder}
     * @param depth Initial nesting depth
     * @return the {@link StringBuilder}
     * @throws NullPointerException if {@code sb} is {code null}
     * @throws IllegalArgumentException if {@code depth} is negative
     */
    default StringBuilder appendTo(final StringBuilder sb, final int depth) {
        try {
            appendTo((Appendable) sb, depth);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb;
    }
}
