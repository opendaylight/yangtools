/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link AbstractBlockBuilder} supporting basic Java constructs.
 */
@NonNullByDefault
abstract sealed class AbstractJavaBlockBuilder<B extends AbstractJavaBlockBuilder<B>> extends AbstractBlockBuilder<B>
        permits BlockBuilder, JavaBlockBuilder {
    /**
     * Append a fragment encapsulated in a Java block. Roughly equivalent to {@code oB().frg(fragment).cB()}.
     *
     * @param fragment the fragment
     * @return this instance
     */
    final B jBlock(final Block.Fragment<? super B> fragment) {
        requireNonNull(fragment);
        fragment.appendTo(oB());
        return cb();
    }

    /**
     * The equivalent of {@code str(Integer.toString(value))}.
     *
     * @param value the value
     * @return this instance
     */
    final B jInt(final int value) {
        buf().append(value);
        return self();
    }

    /**
     * The equivalent of {@code str(Long.toString(value)).str("L")}.
     *
     * @param value the value
     * @return this instance
     */
    final B jLong(final long value) {
        buf().append(value).append('L');
        return self();
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
    final B jStr(final String str) {
        if (str.isEmpty()) {
            buf().append("\"\"");
        } else {
            buf().append('"').append(verifyStr(str)).append('"');
        }
        return self();
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
    final B jString(final String str) {
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
    final B at() {
        buf().append('@');
        return self();
    }

    /**
     * Open a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>.
     * Short name for {@code openBlock}. Emits the equivalent of <pre><code>str(" {").nl()</code></pre>.
     *
     * <p>Methods calling this method are expected to also call the corresponding {@link #cb()} or {@link #cB()}.
     *
     * @return this instance
     */
    final B oB() {
        markNl(incrementIndent(buf().append(" {\n")));
        return self();
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()}. Short name for {@code closeBlock}. Emits the equivalent of
     * <pre><code>str("}")</code></pre>.
     *
     * @return this instance
     */
    final B cb() {
        decrementIndent().append('}');
        return self();
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()}. Short name for {@code closeBlock}. Emits the equivalent of
     * <pre><code>cb().nl()</code></pre>.
     *
     * @return this instance
     */
    final B cB() {
        markNl(decrementIndent().append("}\n"));
        return self();
    }

    /**
     * Append a {@code ";\n"}. Short name for {@code endStatement}.
     *
     * @return this instance
     */
    final B eS() {
        markNl(buf().append(";\n"));
        return self();
    }

    /**
     * The equivalent of {@code str("    ")}. Short name for {@code indent}.
     *
     * @return this instance
     */
    // FIXME: remove this method
    final B ind() {
        buf().append("    ");
        return self();
    }

    /**
     * The equivalent of {@code str("    ").str(str)}. Short name for {@code indent}.
     *
     * @return this instance
     */
    final B ind(final String str) {
        buf().append("    ").append(verifyStr(str));
        return self();
    }
}
