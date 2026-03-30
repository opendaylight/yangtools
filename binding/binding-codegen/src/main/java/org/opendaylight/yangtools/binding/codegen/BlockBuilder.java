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
// FIXME: internalize StringConcatenation
final class BlockBuilder extends StringConcatenation implements Mutable {
    /**
     * Default constructor. Uses {@code "\n"} as {@link #getLineDelimiter()} instead of the platform-dependent
     * {@link StringConcatenation#DEFAULT_LINE_DELIMITER}.
     */
    BlockBuilder() {
        super("\n");
    }

    /**
     * Append a {@code '@'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder at() {
        super.append("@");
        return this;
    }

    /**
     * Append a {@code '\n'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder nl() {
        super.newLine();
        return this;
    }

    /**
     * Append a {@code '\n'}. This method should only used when {@link #nl()} cannot be used.
     */
    @Override
    public void newLine() {
        super.newLine();
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
        super.append(validateStr(content));
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
        super.append(Integer.toString(value));
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
        super.append(requireNonNull(text));
        return this;
    }

    @Override
    public void append(final String str) {
        super.append(requireNonNull(str));
    }

    void append(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            super.append(bb);
        }
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public void append(final StringConcatenation concat) {
        if (concat instanceof BlockBuilder bb) {
            append(bb);
        } else {
            super.append(concat);
        }
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public void append(final StringConcatenation concat, final String indentation) {
        super.append(requireNonNull(concat), requireNonNull(indentation));
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            super.append("    ");
            super.append(bb, "    ");
            super.newLineIfNotEmpty();
        }
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable StringBuilder sb) {
        if (sb != null) {
            super.append("    ");
            super.append(sb.toString(), "    ");
            super.newLineIfNotEmpty();
        }
        return this;
    }

    // FIXME: remove this method
    @NonNull BlockBuilder indentedTwice(final @Nullable StringBuilder sb) {
        if (sb != null) {
            super.append("        ");
            super.append(sb.toString(), "        ");
            super.newLineIfNotEmpty();
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
        return verifyNotNull(super.toString());
    }

    String toJavadocBlock() {
        return isEmpty() ? "" : BaseTemplate.wrapToDocumentation(toRawString());
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
