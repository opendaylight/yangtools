/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.InlineMe;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A non-empty set of {@code '\n'}-separated lines.
 */
@NonNullByDefault
sealed interface Block extends BlockFragment, Immutable permits Block.OfOne, Block2, BlockC, BlockN {
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
     * {@return a new BlockBuilder}
     */
    static BlockBuilder builder() {
        return new BlockBuilder();
    }

    static Block.OfOne ofEmptyLine() {
        return Block1.EMPTY;
    }

    static Block.OfOne ofLine(final String line) {
        return line.isEmpty() ? ofEmptyLine()
            // FIXME: add verification
            : new Block1(line);
    }

    static Block ofLines(final String first, final String second) {
        return first.isEmpty() && second.isEmpty() ? Block2.EMPTY
            // FIXME: add verification
            : new Block2(first + '\n' + second, first.length());
    }

    static Block ofLines(final String first, final String second, final String... others) {
        if (others.length == 0) {
            return ofLines(first, second);
        }

        final var bb = builder().eol(first).eol(second);
        for (var other : others) {
            bb.eol(other);
        }
        return bb.build();
    }

    static Block ofLines(final Iterable<String> lines) {
        return ofLines(lines.iterator());
    }

    static Block ofLines(final Iterator<String> lines) {
        if (!lines.hasNext()) {
            throw new VerifyException("no lines");
        }
        final var first = lines.next();
        if (!lines.hasNext()) {
            return ofLine(first);
        }
        final var second = lines.next();
        if (!lines.hasNext()) {
            return ofLines(first, second);
        }

        final var bb = builder().eol(first).eol(second);
        lines.forEachRemaining(bb::eol);
        return bb.build();
    }

    static Block ofBlocks(final List<Block> blocks) {
        final var size = blocks.size();
        return switch (size) {
            case 0 -> throw new VerifyException("no blocks)");
            case 1 -> requireNonNull(blocks.getFirst());
            default -> new BlockC(blocks);
        };
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

    /**
     * {@return the equivalent of #toRawString()}
     */
    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    @InlineMe(replacement = "this.toRawString()")
    String toString();
}
