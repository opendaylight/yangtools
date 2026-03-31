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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>The set of exposed methods is specifically tailored to callers. We do not use method overloads on purpose, so that
 * there is always a strong tie between then intended semantics and argument types. There may be exceptions to this rule
 * as long as we can provide strong-enough type safety.
 *
 * <p>The intent here is provide a reasonable improvement to {@link StringBuilder}, such as
 * <ul>
 *   <li>short method names to keep concatenations concise</li>
 *   <li>explicit control over end-of-line</li>
 *   <li>simple indentation handling</li>
 * </ul>
 *
 * <p>Methods ending with a capital letter terminate the current line, i.e. return the result of {@link #nl()}. Examples
 * include {@link #oB()}, {@link #cB()}, {@link #oS()}.
 *
 * <p>When deciding on the shape of a method and its name, please consider it first and foremost its stringlu structure,
 * as that is the layer we operate on.
 *
 * <p>We can have some common Java language things coming in, but those should be placed here only on temporary basis
 * until they shape a separate interface for high-level access. Examples include {@code #gen(String)} family of methods.
 */
final class BlockBuilder implements Mutable {
    /**
     * An argument verification implementation.
     */
    @NonNullByDefault
    @CheckReturnValue
    @VisibleForTesting
    abstract static sealed class ArgumentVerifier {
        /**
         * Verify the argument to {@link BlockBuilder#str(String)}.
         *
         * @param arg the argument
         * @return the argument
         */
        abstract String verifyStr(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#str(String)} which is known to be non-empty.
         *
         * @param arg the argument
         * @return the argument
         */
        abstract String verifyNonEmptyStr(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#txt(String)}.
         *
         * @param arg the argument
         * @return the argument
         */
        abstract String verifyTxt(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#txt(String)} known to have a newline at specified offset.
         *
         * @param arg the argument
         * @param nl the offset
         * @return the argument
         */
        abstract String verifyTxt(String arg, int nl);
    }

    /**
     * The fast verifier: we just make sure there are no nulls.
     */
    @VisibleForTesting
    static final class FastVerifier extends ArgumentVerifier {
        private FastVerifier() {
            // Hidden on purpose
        }

        @Override
        String verifyStr(final String arg) {
            return requireNonNull(arg);
        }

        @Override
        String verifyNonEmptyStr(final String arg) {
            return arg;
        }

        @Override
        String verifyTxt(final String arg) {
            return requireNonNull(arg);
        }

        @Override
        String verifyTxt(final String arg, final int nl) {
            return arg;
        }
    }

    /**
     * The strict verifier: we do full argument checks.
     */
    @VisibleForTesting
    static final class StrictVerifier extends ArgumentVerifier {
        private StrictVerifier() {
            // Hidden on purpose
        }

        @Override
        String verifyStr(final String arg) {
            final var nl = arg.indexOf('\n');
            if (nl != -1) {
                throw new VerifyException("newline at offset " + nl + " of '" + arg + "'");
            }
            return verifyNonEmptyStr(arg);
        }

        @Override
        String verifyNonEmptyStr(final String arg) {
            if (arg.isEmpty()) {
                throw new VerifyException("empty str");
            }
            return arg;
        }

        @Override
        String verifyTxt(final String arg) {
            if (arg.isEmpty()) {
                throw new VerifyException("empty txt");
            }
            final var nl = arg.indexOf('\n');
            if (nl == -1) {
                throw new VerifyException("no newline in '" + arg + "'");
            }
            return arg;
        }

        @Override
        String verifyTxt(final String arg, final int nl) {
            verify(nl >= 0);
            return verifyNotNull(arg);
        }
    }

    // FIXME: document this property
    private static final @NonNull String PROP_VERIFY = "odl.binding.codegen.verify";

    /**
     * The run-time constant verification.
     */
    @VisibleForTesting
    static final @NonNull ArgumentVerifier ARGUMENT_VERIFIER = selectArgumentVerifier(
        LoggerFactory.getLogger(BlockBuilder.class), System.getProperty(PROP_VERIFY));

    @VisibleForTesting
    static @NonNull ArgumentVerifier selectArgumentVerifier(final @NonNull Logger log, final @Nullable String prop) {
        return switch (prop) {
            case null -> {
                log.debug("Using fast verification");
                yield new FastVerifier();
            }
            case "false" -> {
                log.info("Using fast verification");
                yield new FastVerifier();
            }
            case "true" -> {
                log.info("Using strict verification");
                yield new StrictVerifier();
            }
            default -> {
                log.warn("Bad {} value '{}', using strict verification", PROP_VERIFY, prop);
                yield new StrictVerifier();
            }
        };
    }

    //
    // Bridge methods through ARGUMENT_VERIFIER. Kept here to keep callers as simple as possible.
    //

    @NonNullByDefault
    @CheckReturnValue
    private static String verifyStr(final String arg) {
        return ARGUMENT_VERIFIER.verifyStr(arg);
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String verifyNonEmptyStr(final String arg) {
        return ARGUMENT_VERIFIER.verifyNonEmptyStr(arg);
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String verifyTxt(final String arg) {
        return ARGUMENT_VERIFIER.verifyTxt(arg);
    }

    @NonNullByDefault
    @CheckReturnValue
    private static String verifyTxt(final String arg, final int nl) {
        return ARGUMENT_VERIFIER.verifyTxt(arg, nl);
    }

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

    @NonNullByDefault
    @CheckReturnValue
    BlockBuilder str(final String firstStr, final @Nullable String secondStr) {
        appendStr(firstStr);
        return secondStr == null ? this : str(secondStr);
    }

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

    // FIXME: convert {@code} to {@snippet}
    /**
     * Append {@code Integer.toString(value)}.
     *
     * @param value the value
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder iStr(final int value) {
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
     * <p>Methods calling this method are expected to also call the corresponding {@link #cB()} or {@link #cS()}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder oB() {
        // FIXME: also add indentation
        buf.append(" {\n");
        return this;
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()}. Short name for {@code closeBlock}. Emits the equivalent of
     * <pre><code>str("}").nl()</code></pre>.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder cB() {
        // FIXME: also add indentation
        buf.append("}\n");
        return this;
    }

    /**
     * Close a new <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-14.html#jls-14.2">Java block</a>
     * previously opened via {@link #oB()} and terminate current statement. Short name for {@code closeStatement}. Emits
     * the equivalent of {@code str("}").eS()}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder cS() {
        // FIXME: also add indentation
        buf.append("};\n");
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
        return this;
    }

    /**
     * Append the contents of an optional {@link BlockBuilder} to this instance if it is not {@code null}.
     *
     * @param source optional {@link BlockBuilder}
     * @return this instance
     */
    // TODO: differentiate a blk(@NonNull Block blk)
    @NonNullByDefault
    BlockBuilder blk(final @Nullable BlockBuilder source) {
        if (source != null) {
            buf.append(source.buf);
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

    // FIXME: remove this method
    void append(final String str) {
        final int nl = str.indexOf('\n');
        buf.append(nl == -1 ? verifyNonEmptyStr(str) : verifyTxt(str, nl));
    }

    // FIXME: remove this method
    void append(final @Nullable StringBuilder src) {
        if (src != null) {
            buf.append(src);
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
                break;
            }

            final var next = nl + 1;
            if (begin == nl) {
                buf.append('\n');
            } else {
                buf.append(indent).append(text, begin, next);
            }
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
        if (buf.isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
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
