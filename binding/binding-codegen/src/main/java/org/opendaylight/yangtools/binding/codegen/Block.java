/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A non-empty set of {@code '\n'}-separated lines.
 */
@NonNullByDefault
sealed interface Block extends BlockFragment, Immutable {
    /**
     * A {@link Block} comprised of a single line.
     */
    sealed interface OfOne extends Block permits Block1 {
        /**
         * {@return the single line, without the terminating newline}
         */
        String line();
    }

    /**
     * A {@link Block} comprised of multiple lines.
     */
    sealed interface OfMore extends Block permits Block2, BlockN {
        // nothing else
    }

    static Block.OfOne ofEmptyLine() {
        return Block1.EMPTY;
    }

    static Block.OfOne ofLine(final String line) {
        // FIXME: add verification
        return line.isEmpty() ? ofEmptyLine() : new Block1(line);
    }

    /**
     * Append this block to an {@link Appendable}.
     *
     * @param appendable the {@link Appendable}
     * @throws IOException if an I/O error occurs
     */
    void appendTo(Appendable appendable) throws IOException;

    @Override
    void appendTo(BlockBuilder bb);

    /**
     * Append this block to a {@link StringBuilder}.
     *
     * @param sb the {@link StringBuilder}
     */
    default void appendTo(final StringBuilder sb) {
        try {
            appendTo((Appendable) sb);
        } catch (IOException e) {
            // should never happen
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@return the raw String representation of this block}
     */
    String toRawString();
}
