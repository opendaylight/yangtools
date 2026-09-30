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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.blk.AbstractBlockBuilder;
import org.opendaylight.yangtools.blk.Block;

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
@NonNullByDefault
final class BlockBuilder extends AbstractBlockBuilder<BlockBuilder> {
    /**
     * Default constructor.
     */
    BlockBuilder() {
        // nothing else
    }

    @Override
    protected BlockBuilder self() {
        return this;
    }

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
    BlockBuilder at() {
        buf().append('@');
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
    BlockBuilder cB() {
        markNl(decrementIndent().append("}\n"));
        return this;
    }

    /**
     * Append a {@code ";\n"}. Short name for {@code endStatement}.
     *
     * @return this instance
     */
    BlockBuilder eS() {
        markNl(buf().append(";\n"));
        return this;
    }

    /**
     * Append the contents of a {@link BlockBuilder} to this instance if it is not {@code null}.
     *
     * @param source optional {@link BlockBuilder}
     * @return this instance
     */
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
    BlockBuilder ind(final String str) {
        buf().append("    ").append(verifyStr(str));
        return this;
    }

    // FIXME: split this out into JavadocBuilder
    String toJavadocBlock() {
        if (isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
    }
}
