/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
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
    //

    private final @NonNull StringBuilder sb = new StringBuilder();

    // FIXME: YANGTOOLS-1831: migrating to equivalent StringBuilder is sufficient
    private final @NonNull StringConcatenation sc = new StringConcatenation("\n");

    /**
     * Append a {@code '@'}.
     *
     * @return this instance
     */
    @CheckReturnValue
    @NonNull BlockBuilder at() {
        sb.append('@');
        verifyAppend("@");
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
        sb.append('\n');
        sc.newLine();
        verifyContent();
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
        final var str = validateStr(content);
        sb.append(str);
        verifyAppend(str);
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
        final var str = Integer.toString(value);
        sb.append(str);
        verifyAppend(str);
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
        final var str = validateTxt(text);
        sb.append(str);
        verifyAppend(str);
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
        final var content = nl == -1 ? validateStr(str) : validateTxt(str);
        sb.append(content);
        verifyAppend(content);
    }

    // FIXME: remove this method
    void append(final @Nullable StringBuilder src) {
        if (src != null) {
            sb.append(src);
            verifyAppend(src.toString());
        }
    }

    void append(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            sb.append(bb.sb);
            verifyAppend(bb.sb.toString());
        }
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

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @NonNull String prefix, final @Nullable StringBuilder sb) {
        if (sb != null) {
            sc.append("    ");
            sc.append(requireNonNull(prefix));
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

    private void verifyAppend(final String str) {
        sc.append(requireNonNull(str));
        verifyContent();
    }

    private void verifyContent() {
        final var sbStr = sb.toString();
        final var scStr = sc.toString();
        verify(sbStr.equals(scStr), "mismatched sb='%s' sc='%s'", sbStr, scStr);
    }
}
