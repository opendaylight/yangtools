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

/**
 * Default implementation of {@link Block.Builder}. Methods ending with a capital letter terminate the current line,
 * i.e. return the result of {@link #nl()}. Examples include {@link #oB()}, {@link #cB()}, {@link #eS()}.
 *
 * <p>When deciding on the shape of a method and its name, please consider it first and foremost its stringlu structure,
 * as that is the layer we operate on.
 *
 * <p>We can have some common Java language things coming in, but those should be placed here only on temporary basis
 * until they shape a separate interface for high-level access. Examples include {@code #gen(String)} family of methods.
 */
final class BlockBuilder extends Block.Builder {
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

    // current block, containing newline-separated lines
    private final @NonNull StringBuilder buf = new StringBuilder();
    // offset of the start of the current line, i.e. one past the last known newline in current block
    private int currentLine;
    // offset of the start of the second line, i.e. the one past the first newline in current block
    private int secondLine = -1;

    BlockBuilder() {
        // nothing else
    }

    @Override
    BlockBuilder frg(final BlockFragment fragment) {
        if (fragment != null) {
            fragment.appendTo(this);
        }
        return this;
    }

    @Override
    BlockBuilder nl() {
        newLine();
        return this;
    }

    @Override
    void newLine() {
        buf.append('\n');
        markNl();
    }

    private void markNl() {
        final var nextLine = buf.length();
        if (secondLine == -1) {
            secondLine = nextLine;
        }
        currentLine = nextLine;
    }

    @Override
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

    @Override
    BlockBuilder txt(final String text) {
        verifyEmptyLine();
        final var verified = verifyTxt(text);
        return secondLine != -1 ? txtImpl(verified) : txtSlow(verified);
    }

    @NonNullByDefault
    private BlockBuilder txtImpl(final String text) {
        buf.append(text);
        currentLine = buf.length();
        return this;
    }

    @NonNullByDefault
    private BlockBuilder txtSlow(final String text) {
        secondLine = buf.length() + text.indexOf('\n') + 1;
        return txtImpl(text);
    }

    @NonNullByDefault
    private void appendStr(final String str) {
        buf.append(verifyStr(str));
    }

    @Override
    BlockBuilder eol(final String content) {
        return str(content).nl();
    }

    @Override
    BlockBuilder eol(final String str, final int beginIndex, final int endIndex) {
        return eol(str.substring(beginIndex, endIndex));
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
     * Append a string as a Java
     * <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.10.5">String Literal</a>. The
     * argument is expected to not need escaping.
     *
     * @param str the string, already escaped or not needing escaping
     * @return this instance
     * @see #jString(String)
     */
    @NonNullByDefault
    BlockBuilder jStr(final String str) {
        buf.append('"').append(verifyStr(str)).append('"');
        return this;
    }

    /**
     * Append a string as a Java
     * <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.10.5">String Literal</a>, performing
     * any escaping if needed.
     *
     * @param str the string
     * @return this instance
     * @see #jStr(String)
     */
    @NonNullByDefault
    BlockBuilder jString(final String str) {
        // FIXME: this is our sole dependency on commons-text: can we do something simple instead?
        return jStr(StringEscapeUtils.escapeJava(str));
    }

    // FIXME: add jText() which will format a string into a text block as per JLS 3.10.6

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

    @Override
    BlockBuilder blk(final Block blk) {
        verifyEmptyLine();
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
        verifyEmptyLine();
        if (source != null) {
            final var sb = source.buf;
            if (!sb.isEmpty()) {
                final var scl = source.currentLine;
                // TODO: optimize with secondLine
                if (scl != 0) {
                    txt(sb.substring(0, scl));
                }
                buf.append(sb, scl, sb.length());
            }
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


    @Override
    Block build() {
        final var length = buf.length();
        if (length == 0) {
            throw new VerifyException("empty block");
        }
        return build(length);
    }

    @NonNullByDefault
    private Block build(final int length) {
        if (length != currentLine) {
            throw new VerifyException("unterminated line " + buf.substring(currentLine));
        }

        final var str = buf.substring(0, length - 1);
        if (length == secondLine) {
            return str.isEmpty() ? Block1.EMPTY : new Block1(str);
        }
        return new BlockN(str);
    }

    @Override
    Block toBlock() {
        final var length = buf.length();
        return length == 0 ? null : build(length);
    }

    // FIXME: split this out into JavadocBuilder
    String toJavadocBlock() {
        if (buf.isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
    }

    @Override
    public String toRawString() {
        return verifyNotNull(buf.toString());
    }

    private void verifyEmptyLine() {
        if (currentLine != buf.length()) {
            throw new VerifyException("trailing content '" + buf.substring(currentLine) + "'");
        }
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
