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

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.slf4j.LoggerFactory;

/**
 * Universal builder of a string block. A block is composed of one or more lines, concatenated using {@code '\n'}.
 *
 * <p>The set of exposed methods is specifically tailored to callers. We do not use method overloads on purpose, so that
 * there is always a strong tie between then intended semantics and argument types.
 */
final class BlockBuilder implements Mutable {
    /**
     * Argument verification indirection, allowing only two implementations.
     */
    private abstract static sealed class ArgumentVerifier {
        /**
         * Verify the argument to {@link BlockBuilder#str(String)}.
         *
         * @param arg the argument
         * @return the argument
         */
        @NonNullByDefault
        @CheckReturnValue
        abstract String verifyStr(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#str(String)} which is known to be non-empty.
         *
         * @param arg the argument
         * @return the argument
         */
        @NonNullByDefault
        @CheckReturnValue
        abstract String verifyNonEmptyStr(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#txt(String)}.
         *
         * @param arg the argument
         * @return the argument
         */
        @NonNullByDefault
        @CheckReturnValue
        abstract String verifyTxt(String arg);

        /**
         * Verify the argument to {@link BlockBuilder#txt(String)} known to have a newline at specified offset.
         *
         * @param arg the argument
         * @param nl the offset
         * @return the argument
         */
        @NonNullByDefault
        @CheckReturnValue
        abstract String verifyTxt(String arg, int nl);

        /**
         * The fast verifier: we just make sure there are no nulls.
         */
        private static final class Fast extends ArgumentVerifier {
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
        private static final class Strict extends ArgumentVerifier {
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
                    throw new VerifyException("empty str()");
                }
                return arg;
            }

            @Override
            String verifyTxt(final String arg) {
                if (arg.isEmpty()) {
                    throw new VerifyException("empty txt()");
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
    }

    private static final @NonNull ArgumentVerifier ARGUMENT_VERIFIER;

    static {
        final var logger = LoggerFactory.getLogger(BlockBuilder.class);
        // FIXME: document this property
        final var verify = Boolean.getBoolean("odl.binding.codegen.verify");
        if (verify) {
            logger.info("using strict verification");
            ARGUMENT_VERIFIER = new ArgumentVerifier.Strict();
        } else {
            logger.info("using fast verification");
            ARGUMENT_VERIFIER = new ArgumentVerifier.Fast();
        }
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
     * The equivalent of {@code str(";").nl()}.
     *
     * @return this instance
     */
    @NonNullByDefault
    BlockBuilder eS() {
        buf.append(";\n");
        return this;
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
    @CheckReturnValue
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
        buf.append(ARGUMENT_VERIFIER.verifyStr(str));
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
        buf.append(ARGUMENT_VERIFIER.verifyTxt(text));
        return this;
    }

    @NonNullByDefault
    BlockBuilder oB() {
        // FIXME: also add indentation
        buf.append(" {\n");
        return this;
    }

    @NonNullByDefault
    BlockBuilder cB() {
        // FIXME: also add indentation
        buf.append("}\n");
        return this;
    }

    @NonNullByDefault
    BlockBuilder oS() {
        return oB();
    }

    @NonNullByDefault
    BlockBuilder cS() {
        // FIXME: also add indentation
        buf.append("};\n");
        return this;
    }

    @NonNullByDefault
    BlockBuilder blk(final @Nullable BlockBuilder bb) {
        if (bb != null) {
            buf.append(bb.buf);
        }
        return this;
    }


    // FIXME: remove this method
    void append(final String str) {
        final int nl = str.indexOf('\n');
        buf.append(nl == -1 ? ARGUMENT_VERIFIER.verifyNonEmptyStr(str) : ARGUMENT_VERIFIER.verifyTxt(str, nl));
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
