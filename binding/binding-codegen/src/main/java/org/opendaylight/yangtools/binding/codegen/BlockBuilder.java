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

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A builder of a {@link Block}. The set of exposed methods is specifically tailored to callers. We do not use method
 * overloads on purpose, so that there is always a strong tie between then intended semantics and argument types. There
 * may be exceptions to this rule as long as we can provide strong-enough type safety.
 *
 * <p>The intent here is provide a reasonable improvement to {@link StringBuilder}, such as
 * <ul>
 *   <li>short method names to keep concatenations concise</li>
 *   <li>explicit control over end-of-line</li>
 *   <li>simple indentation handling</li>
 * </ul>
 *
 * <p>Methods ending with a capital letter terminate the current line, i.e. return the result of {@link #nl()}. Examples
 * include {@link #oB()}, {@link #cB()}, {@link #eS()}.
 *
 * <p>When deciding on the shape of a method and its name, please consider it first and foremost its stringlu structure,
 * as that is the layer we operate on.
 *
 * <p>We can have some common Java language things coming in, but those should be placed here only on temporary basis
 * until they shape a separate interface for high-level access. Examples include {@code #gen(String)} family of methods.
 */
final class BlockBuilder implements Mutable {
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
    //    int firstNl;      // offset of first known newline in current block, for quick single-line check

    // current block
    private final @NonNull StringBuilder buf = new StringBuilder();
    // offset of the start of the current line, i.e. one past the last known newline in current block
    private int currentLine;

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
     * Append a {@code ' '}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder sp() {
        buf.append(' ');
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
        markNl();
    }

    private void markNl() {
        currentLine = buf.length();
    }

    /**
     * Append a {@link String} simple string. The string has to be known to:
     * <ul>
     *    <li>be non-empty</li>
     *    <li>not contain new lines</li>
     * </ul>
     *
     * @param str the {@link String}
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder str(final String str) {
        appendStr(str);
        return this;
    }

    // FIXME: remove this method
    @Deprecated
    @NonNullByDefault
    BlockBuilder str(final @Nullable StringBuilder sb) {
        if (sb != null) {
            appendStr(sb.toString());
        }
        return this;
    }

    @NonNullByDefault
    private void appendStr(final String str) {
        buf.append(verifyStr(str));
    }

    @NonNullByDefault
    BlockBuilder jBlock(final BlockFragment fragment) {
        fragment.appendTo(oB());
        return cb();
    }

    /**
     * The equivalent of {@code str(Integer.toString(value))}.
     *
     * @param value the value
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder jInt(final int value) {
        buf.append(value);
        return this;
    }

    /**
     * The equivalent of {@code str(Long.toString(value)).str("L")}.
     *
     * @param value the value
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder jLong(final long value) {
        buf.append(value).append('L');
        return this;
    }

    /**
     * The equivalent of {@code str(content).nl()}.
     *
     * @param content the {@link String}
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder eol(final String content) {
        return str(content).nl();
    }

    @NonNullByDefault
    BlockBuilder eol(final String str, final int beginIndex, final int endIndex) {
        return eol(str.substring(beginIndex, endIndex));
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
        buf.append(verifyTxt(text));
        return this;
    }

    /**
     * Open a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>.
     * Short name for {@code openBlock}. Emits the equivalent of <pre><code>str(" {").nl()</code></pre>.
     *
     * <p>Methods calling this method are expected to also call the corresponding {@link #cb()} or {@link #cB()}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder oB() {
        buf.append(" {\n");
        markNl();
        // FIXME: adjust indentation
        return this;
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()}. Short name for {@code closeBlock}. Emits the equivalent of
     * <pre><code>str("}")</code></pre>.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder cb() {
        // FIXME: add indentation
        buf.append('}');
        // FIXME: adjust indentation
        return this;
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()}. Short name for {@code closeBlock}. Emits the equivalent of
     * <pre><code>cb().nl()</code></pre>.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder cB() {
        // FIXME: add indentation
        buf.append("}\n");
        markNl();
        // FIXME: adjust indentation
        return this;
    }

    /**
     * Append a {@code ";\n". Short name for {@code endStatement}}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder eS() {
        buf.append(";\n");
        markNl();
        return this;
    }

    /**
     * Append the contents of a {@link Block} to this instance if it is not {@code null}.
     *
     * @param blk optional {@link Block}
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder blk(final @Nullable Block blk) {
        if (blk != null) {
            blk.appendTo(this);
        }
        return this;
    }

    /**
     * Append the contents of a {@link BlockBuilder} to this instance if it is not {@code null}.
     *
     * @param source optional {@link BlockBuilder}
     * @return this instance
     */
    // FIXME: consider a new name:
    //        - this does the equivalent of frg(BlockFragment) in that it merges the states
    //        - it would be natural to perform BlockBuilder.build().appendTo(this),
    //        the two have different semantics and we should probably do the latter -- and perhaps have a
    //        'BlockFragment toFragment()' method.
    @NonNullByDefault
    BlockBuilder blk(final @Nullable BlockBuilder source) {
        if (source != null) {
            final var sb = source.buf;
            if (!sb.isEmpty()) {
                final var scl = source.currentLine;
                if (scl != 0) {
                    txt(sb.substring(0, scl));
                }
                buf.append(sb, scl, sb.length());
            }
        }
        return this;
    }

    /**
     * Append the contents of a {@link BlockFragment} to this instance if it is not {@code null}.
     *
     * @param fragment optional {@link BlockFragment}
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder frg(final @Nullable BlockFragment fragment) {
        if (fragment != null) {
            fragment.appendTo(this);
        }
        return this;
    }

    /**
     * Append type reference parameterized with specified generic type argument. Short name for {@code generic}.
     * Shorthand for {@code str(rawType).str("<").str(args).str(">")}.
     *
     * @param rawType the raw type
     * @param arg the sole generic argument
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder gen(final String rawType, final String arg) {
        startGen(rawType, arg);
        return endGen();
    }

    /**
     * Append type reference parameterized with specified generic type arguments. Short name for {@code generic}.
     * Shorthand for {@code str(rawType).str("<").str(arg0).str(", ").str(arg1).str(">")}.
     *
     * @param rawType the raw type
     * @param arg0 the first generic argument
     * @param arg1 the second generic argument
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder gen(final String rawType, final String arg0, final String arg1) {
        startGen(rawType, arg0);
        buf.append(", ").append(verifyStr(arg1));
        return endGen();
    }

    @NonNullByDefault
    private void startGen(final String rawType, final String args) {
        buf.append(verifyStr(rawType)).append('<').append(verifyStr(args));
    }

    @NonNullByDefault
    private BlockBuilder endGen() {
        buf.append('>');
        return this;
    }

    /**
     * The equivalent of {@code str("    ")}. Short name for {@code indent}.
     *
     * @return this instance
     */
    // FIXME: remove this method
    @NonNullByDefault
    BlockBuilder ind() {
        buf.append("    ");
        return this;
    }

    /**
     * The equivalent of {@code str("    ").str(str)}. Short name for {@code indent}.
     *
     * @return this instance
     */
    // FIXME: remove this method
    @NonNullByDefault
    BlockBuilder ind(final String str) {
        buf.append("    ").append(verifyStr(str));
        return this;
    }

    @NonNullByDefault
    BlockBuilder quoted(final String str) {
        buf.append('"').append(verifyStr(str)).append('"');
        return this;
    }

    @NonNullByDefault
    BlockBuilder quotedJava(final String str) {
        // FIXME: this is our sole dependency on commons-text: can we do something simple instead?
        final var escaped = StringEscapeUtils.escapeJava(verifyStr(str));
        buf.append('"').append(escaped).append('"');
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @Nullable BlockBuilder bb) {
        if (bb != null && !bb.buf.isEmpty()) {
            indented("    ", bb.buf.toString());
        }
        return this;
    }

    // FIXME: clarify contract
    @NonNull BlockBuilder indented(final @NonNull String prefix, final @Nullable BlockBuilder bb) {
        if (bb != null) {
            buf.append("    ").append(requireNonNull(prefix));
            indented("    ", bb.buf.toString());
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
                break;
            }

            final var next = nl + 1;
            if (begin == nl) {
                newLine();
            } else {
                buf.append(indent).append(text, begin, next);
                markNl();
            }
            begin = next;
        } while (begin < len);
    }

    // FIXME: remove this method
    @NonNull BlockBuilder indentedTwice(final @Nullable BlockBuilder sb) {
        if (sb != null) {
            final var str = sb.toRawString();
            if (!str.isEmpty()) {
                indented("        ", str);
            }
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

    /**
     * {@return a {@link Block} capturing the current state of this builder, or {@code null} if this builder is empty}
     */
    @Nullable Block toBlock() {
        final var length = buf.length();
        return length == 0 ? null : build(length);
    }

    @NonNull String toRawString() {
        return verifyNotNull(buf.toString());
    }

    String toJavadocBlock() {
        if (buf.isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
    }

    /**
     * {@return a {@link Block} capturing the current state of this builder}
     */
    @NonNullByDefault
    Block build() {
        final var length = buf.length();
        if (length == 0) {
            throw new VerifyException("empty block");
        }
        return build(length);
    }

    @NonNullByDefault
    private Block build(final int length) {
        if (currentLine != length) {
            throw new VerifyException("unterminated line " + buf.substring(currentLine));
        }
        if (currentLine == 1) {
            return Block.ofEmptyLine();
        }

        // FIXME: implement this method
        throw new UnsupportedOperationException("not implemented yet");
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

    //
    // Bridge methods to ArgumentVerifier. Kept here to keep callers as simple as possible.
    //
    @NonNullByDefault
    @CheckReturnValue
    private static String verifyStr(final String arg) {
        return ArgumentVerifier.INSTANCE.verifyStr(arg);
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String verifyTxt(final String arg) {
        return ArgumentVerifier.INSTANCE.verifyTxt(arg);
    }
}
