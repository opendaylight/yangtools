/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

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
final class BlockBuilder extends AbstractBlockBuilder<BlockBuilder> {
    BlockBuilder() {
        // nothing else
    }

    @Override
    BlockBuilder self() {
        return this;
    }

    @Override
    BlockBuilder frg(final BlockFragment fragment) {
        if (fragment != null) {
            fragment.appendTo(this);
        }
        return this;
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
        buf().append(value);
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
        buf().append(value).append('L');
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
        if (str.isEmpty()) {
            buf().append("\"\"");
        } else {
            buf().append('"').append(verifyStr(str)).append('"');
        }
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
        buf().append('@');
        return this;
    }

    /**
     * Append a {@code ' '}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder sp() {
        buf().append(' ');
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
        markNl(incrementIndent(buf().append(" {\n")));
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
        decrementIndent().append('}');
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
        markNl(decrementIndent().append("}\n"));
        return this;
    }

    /**
     * Append a {@code ";\n"}. Short name for {@code endStatement}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder eS() {
        markNl(buf().append(";\n"));
        return this;
    }

    /**
     * Append the contents of a {@link Block.Builder} to this instance if it is not {@code null}.
     *
     * @param source optional {@link Block.Builder}
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder blk(final Block.@Nullable Builder source) {
        verifyEmptyLine();
        if (source != null) {
            final var blk = source.toBlock();
            if (blk != null) {
                blk.appendTo(this);
            }
        }
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
        buf().append("    ");
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
        buf().append("    ").append(verifyStr(str));
        return this;
    }

    // FIXME: split this out into JavadocBuilder
    String toJavadocBlock() {
        if (buf.isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
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
