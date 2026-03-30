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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>The set of exposed methods is specifically tailored to callers. We do not use method overloads on purpose, so that
 * there is always a strong tie between then intended semantics and argument types.
 */
final class BlockBuilder implements Mutable {
    // FIXME: replace with a StringBuilder-based state machine
    //
    // The idea is that we start with an empty StringBuilder and as we receive events we decide what to do next.
    // Typically this will be just a simple append, but we also need to track indentation.
    //
    // Overall, the core state should look something like:
    //
    //    // indent + block content
    //    sealed interface Blk {
    //
    //        int indent();
    //    }
    //
    //    List<Blk> blocks; // completed blocks, with optional coalescence when indent matches
    //    int indent;       // current indent
    //    StringBuilder sb; // current block
    //    int firstNl;      // offset of first known newline in current block, for quick single-line check
    //    int lastNl;       // offset of the last known newline in current block, for quick complete-line check

    private final @NonNull StringBuilder buf = new StringBuilder();

    /**
     * Append a {@code '@'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder at() {
        buf.append('@');
        return this;
    }

    /**
     * Append a {@code '\n'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder nl() {
        newLine();
        return this;
    }

    /**
     * Append a {@code '\n'}. This method should only used when {@link #nl()} cannot be used.
     */
    void newLine() {
        buf.append('\n');
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
        buf.append(validateStr(content));
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
    BlockBuilder strI(final int value) {
        buf.append(value);
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
    BlockBuilder txt(final String text) {
        buf.append(validateTxt(text));
        return this;
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String validateTxt(final String txtArg) {
        // TODO: JVM-global flag to enforce content to be non-empty and not contain new lines
        return requireNonNull(txtArg);
    }

    // FIXME: remove this method
    void append(final String str) {
        final int nl = str.indexOf('\n');
        buf.append(nl == -1 ? validateStr(str) : validateTxt(str));
    }

    // FIXME: remove this method
    void append(final @Nullable StringBuilder src) {
        if (src != null) {
            buf.append(src);
        }
    }

    void append(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            buf.append(bb.buf);
        }
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable BlockBuilder bb) {
        if (bb != null && !bb.buf.isEmpty()) {
            indented("    ", bb.buf.toString());
        }
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable StringBuilder sb) {
        if (sb != null && !sb.isEmpty()) {
            indented("    ", sb.toString());
        }
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @NonNull String prefix, final @Nullable StringBuilder sb) {
        if (sb != null) {
            buf.append("    ").append(requireNonNull(prefix));
            indented("    ", sb.toString());
        }
        return this;
    }

    @NonNullByDefault
    private void indented(final String indent, final String text) {
        final var len = text.length();
        if (len == 0) {
            // no-op
            return;
        }

        int begin = 0;
        do {
            int nl = text.indexOf('\n', begin);
            if (nl == -1) {
                buf.append(indent).append(text, begin, len);
                return;
            }
            if (begin == nl) {
                buf.append('\n');
                begin = nl + 1;
                continue;
            }

            final var next = nl + 1;
            buf.append(indent).append(text, begin, next);
            begin = next;
        } while (begin < len);
    }

    // FIXME: remove this method
    @NonNull BlockBuilder indentedTwice(final @Nullable StringBuilder sb) {
        if (sb != null && !sb.isEmpty()) {
            indented("        ", sb.toString());
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
        return verifyNotNull(buf.toString());
    }

    String toJavadocBlock() {
        return buf.isEmpty() ? "" : BaseTemplate.wrapToDocumentation(toRawString());
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
