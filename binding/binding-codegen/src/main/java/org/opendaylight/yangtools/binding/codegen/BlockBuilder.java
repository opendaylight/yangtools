/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>Currently it is just a {@link StringConcatenation} but it will expand as we integrate more users.
 */
final class BlockBuilder implements Mutable {
    private final @NonNull StringConcatenation sc = new StringConcatenation("\n");

    /**
     * Append a {@code '@'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder at() {
        sc.append("@");
        return this;
    }

    /**
     * Append a {@code '\n'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder nl() {
        sc.newLine();
        return this;
    }

    /**
     * Append a {@code '\n'}. This method should only used when {@link #nl()} cannot be used.
     */
    void newLine() {
        sc.newLine();
    }

    /**
     * Append a {@link String} simple string. The string has to be known to:
     * <ul>
     *    <li>be non-empty</li>
     *    <li>not contain new lines</li>
     * </ul>
     *
     * @param content the {@link String}
     * @return this instance
     */
    @NonNullByDefault
    @CheckReturnValue
    BlockBuilder str(final String content) {
        sc.append(validateStr(content));
        return this;
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String validateStr(final String strArg) {
        // TODO: JVM-global flag to enforce content to be non-empty and not contain new lines
        return requireNonNull(strArg);
    }

    /**
     * Append {@code Integer.toString(value}.
     *
     * @param value the value
     * @return this instance
     */
    @NonNullByDefault
    @CheckReturnValue
    BlockBuilder strI(final int value) {
        sc.append(Integer.toString(value));
        return this;
    }

    /**
     * The equivalent of {@code str(content).nl()}.
     *
     * @param content the {@link String}
     * @return this instance
     */
    @NonNullByDefault
    @CheckReturnValue
    BlockBuilder eol(final String content) {
        return str(content).nl();
    }

    /**
     * Append a text block. The string has to be known to:
     * <ul>
     *    <li>be non-empty</li>
     *    <li>contain one or more new lines</li>
     * </ul>
     *
     * @param text the {@link String}
     * @return this instance
     */
    @NonNullByDefault
    @CheckReturnValue
    BlockBuilder txt(final String text) {
        sc.append(requireNonNull(text));
        return this;
    }

    // FIXME: remove this method
    void append(final String str) {
        sc.append(requireNonNull(str));
    }

    // FIXME: remove this method
    void append(final CharSequence str) {
        switch (str) {
            case String match -> sc.append(match);
            case StringConcatenation match -> sc.append(match);
            default -> sc.append(str.toString());
        }
    }

    void append(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            sc.append(bb);
        }
    }

    @DoNotCall
    @Deprecated(forRemoval = true)
    void append(final StringConcatenation concat) {
        sc.append(concat);
    }

    @DoNotCall
    @Deprecated(forRemoval = true)
    void append(final StringConcatenation concat, final String indentation) {
        sc.append(requireNonNull(concat), requireNonNull(indentation));
    }

    @Deprecated(forRemoval = true)
    void newLineIfNotEmpty() {
        sc.newLineIfNotEmpty();
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            sc.append("    ");
            sc.append(bb, "    ");
            sc.newLineIfNotEmpty();
        }
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable StringBuilder sb) {
        if (sb != null) {
            sc.append("    ");
            sc.append(sb.toString(), "    ");
            sc.newLineIfNotEmpty();
        }
        return this;
    }

    // FIXME: remove this method
    @NonNull BlockBuilder indentedTwice(final @Nullable StringBuilder sb) {
        if (sb != null) {
            sc.append("        ");
            sc.append(sb.toString(), "        ");
            sc.newLineIfNotEmpty();
        }
        return this;
    }

    // FIXME: something like, but perhaps that is part of a JavaBlockBuilder along with importedName() et al.
    //    @CheckReturnValue
    //    @NonNull BlockBuilder fieldName(final GeneratedProperty property) {
    //        super.append("_");
    //        super.append(property.getName());
    //        return this;
    //    }

    @NonNull String toRawString() {
        return verifyNotNull(sc.toString());
    }

    String toJavadocBlock() {
        return sc.isEmpty() ? "" : BaseTemplate.wrapToDocumentation(toRawString());
    }

    /**
     * {@return the result of {@link #toRawString()}}
     * @deprecated use {@link #toRawString()} directly
     */
    @Override
    @Deprecated(forRemoval = true)
    public String toString() {
        return toRawString();
    }
}
