/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.blk;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.InlineMe;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A non-empty set of {@code '\n'}-separated lines.
 *
 * @since 16.1.1
 */
@NonNullByDefault
public sealed interface Block extends Immutable permits Block.OfOne, Block2, BlockC, BlockN {
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
     * A builder of a {@link Block}. The set of exposed methods is specifically tailored to callers. We do not use
     * method overloads on purpose, so that there is always a strong tie between then intended semantics and argument
     * types. There may be exceptions to this rule as long as we can provide strong-enough type safety.
     *
     * <p>The intent here is provide a reasonable improvement to {@link StringBuilder}, such as
     * <ul>
     *   <li>short method names to keep concatenations concise</li>
     *   <li>explicit control over end-of-line</li>
     *   <li>simple indentation handling</li>
     * </ul>
     *
     * <p>This interface is mean to be further specialized to a domain-specific subclass of
     * {@link AbstractBlockBuilder} and the corresponding {@link Fragment} interface.
     */
    sealed interface Builder extends Mutable permits AbstractBlockBuilder {
        /**
         * Append the contents of a {@link Block} to this instance if it is not {@code null}. The there must not be any
         * content on the current line.
         *
         * @param blk optional {@link Block}
         * @return this instance
         */
        Builder blk(@Nullable Block blk);

        /**
         * Append a {@code '\n'}. This method should only used when {@link #nl()} cannot be used.
         */
        void newLine();

        /**
         * Append a {@code '\n'}. Short name for {@code new line}.
         *
         * @return this instance
         */
        @CheckReturnValue
        Builder nl();

        /**
         * Append a {@code '>'}. Short name for {@code greater than}.
         *
         * @return this instance
         */
        Builder gt();

        /**
         * Append a {@code '<'}. Short name for {@code less than}.
         *
         * @return this instance
         */
        Builder lt();

        /**
         * Append a {@code ", "}. Short name for {@code comma, space}.
         *
         * @return this instance
         */
        Builder cs();

        /**
         * Append a {@code ' '}.
         *
         * @return this instance
         */
        Builder sp();

        /**
         * Append a {@link String} simple string. The string has to be known to:
         * <ul>
         *    <li>to be non-empty</li>
         *    <li>to not contain new lines</li>
         * </ul>
         *
         * @param str the {@link String}
         * @return this instance
         */
        Builder str(String str);

        /**
         * Append a text block. The string has to be known:
         * <ul>
         *    <li>to be non-empty</li>
         *    <li>end with a new line</li>
         * </ul>
         *
         * <p>The there must not be any content on the current line.
         *
         * @param text the {@link String}
         * @return this instance
         */
        Builder txt(String text);

        /**
         * The equivalent of {@code str(content).nl()}.
         *
         * @param content the {@link String}
         * @return this instance
         */
        Builder eol(String content);

        /**
         * The equivalent of {@code eol(str.substring(beginIndex, endIndex))}.
         *
         * @param str the {@link String}
         * @param beginIndex the beginning index, inclusive
         * @param endIndex the ending index, exclusive
         * @return this instance
         */
        @Beta
        Builder eol(String str, int beginIndex, int endIndex);

        /**
         * {@return a {@link Block} capturing the current state of this builder}
         */
        Block build();

        /**
         * {@return a {@link Block} capturing the current state of this builder, or {@code null} if this builder is
         * empty}
         */
        @Nullable Block toBlock();

        /**
         * {@return the raw string literal equivalent of this builder's state}
         */
        String toRawString();

        /**
         * {@return the result of {@link #toRawString()}}
         *
         * @deprecated use {@link #toRawString()} directly
         */
        @Override
        @DoNotCall
        @Deprecated(forRemoval = true)
        @InlineMe(replacement = "this.toRawString()")
        String toString();
    }

    /**
     * A fragment of domain-specific {@link Builder}. All it can do is {@link #appendTo(Builder)} itself to that builder
     * type.
     *
     * @param <B> the block builder type
     * @param <F> the fragment type
     */
    @FunctionalInterface
    interface Fragment<B extends Block.Builder, F extends Fragment<B, F>> {
        /**
         * Append this fragment to the corresponding block builder.
         *
         * @param bb the block builder
         */
        void appendTo(B bb);
    }

    /**
     * {@return a new Block.Builder}
     *
     * @see RawBlockBuilder
     */
    static Block.Builder builder() {
        return new RawBlockBuilder();
    }

    static Block.OfOne ofEmptyLine() {
        return Block1.EMPTY;
    }

    static Block.OfOne ofLine(final String line) {
        return line.isEmpty() ? ofEmptyLine() : new Block1(ArgumentVerifier.verifyNonEmpty(line));
    }

    static Block ofLines(final String first, final String second) {
        return first.isEmpty() && second.isEmpty() ? Block2.EMPTY
            // FIXME: add verification
            : new Block2(first + '\n' + second, first.length());
    }

    static Block ofLines(final String first, final String second, final @NonNull String... others) {
        if (others.length == 0) {
            return ofLines(first, second);
        }

        final var sb = new StringBuilder();
        appendLine(sb, first);
        appendLine(sb, second);
        for (var other : others) {
            appendLine(sb, other);
        }
        return buildBlock(sb);
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

        final var sb = new StringBuilder();
        appendLine(sb, first);
        appendLine(sb, second);
        do {
            appendLine(sb, lines.next());
        } while (lines.hasNext());
        return buildBlock(sb);
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
     * @param out the {@link Appendable}
     * @throws IOException if an I/O error occurs
     */
    void appendTo(Appendable out) throws IOException;

    /**
     * Append this block to a {@link Builder}.
     *
     * @param bb the {@link Builder}
     */
    void appendTo(Block.Builder bb);

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
     * {@return the result of {@link #toRawString()}}
     *
     * @deprecated use {@link #toRawString()} directly
     */
    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    String toString();

    private static void appendLine(final StringBuilder sb, final String line) {
        if (!line.isEmpty()) {
            sb.append(ArgumentVerifier.verifyNonEmpty(line));
        }
        sb.append('\n');
    }

    private static BlockN buildBlock(final StringBuilder sb) {
        return new BlockN(sb.substring(0, sb.length() - 1));
    }
}
