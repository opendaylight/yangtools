/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRArgument.Concatenation;
import org.opendaylight.yangtools.yang.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Utilities for dealing with YANG statement argument strings, encapsulated in ANTLR grammar's ArgumentContext.
 */
abstract class ArgumentContextUtils {
    /**
     * YANG 1.0 version of strings, which were not completely clarified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.1.3">RFC6020</a>.
     */
    private static final class RFC6020 extends ArgumentContextUtils {
        private static final @NonNull RFC6020 INSTANCE = new RFC6020();

        @Override
        void checkDoubleQuoted(final String str, final StatementSourceReference ref, final int backslash) {
            // No-op
        }

        @Override
        void checkUnquoted(final String str, final StatementSourceReference ref) {
            // No-op
        }
    }

    /**
     * YANG 1.1 version of strings, which were clarified in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-6.1.3">RFC7950</a>.
     */
    // NOTE: the differences clarified lead to a proper ability to delegate this to ANTLR lexer, but that does not
    //       understand versions and needs to work with both.
    private static final class RFC7950 extends ArgumentContextUtils {
        private static final CharMatcher ANYQUOTE_MATCHER = CharMatcher.anyOf("'\"");
        private static final @NonNull RFC7950 INSTANCE = new RFC7950();

        @Override
        void checkDoubleQuoted(final String str, final StatementSourceReference ref, final int backslash) {
            if (backslash < str.length() - 1) {
                int index = backslash;
                while (index != -1) {
                    final var escape = str.charAt(index + 1);
                    index = switch (escape) {
                        case 'n', 't', '\\', '\"' -> str.indexOf('\\', index + 2);
                        default -> throw new SourceException(ref, """
                            YANG 1.1: illegal double quoted string (%s). In double quoted string the backslash must be \
                            followed by one of the following character [n,t,",\\], but was '%s'.""", str, escape);
                    };
                }
            }
        }

        @Override
        void checkUnquoted(final String str, final StatementSourceReference ref) {
            SourceException.throwIf(ANYQUOTE_MATCHER.matchesAnyOf(str), ref,
                "YANG 1.1: unquoted string (%s) contains illegal characters", str);
        }
    }

    private ArgumentContextUtils() {
        // Hidden on purpose
    }

    static @NonNull ArgumentContextUtils forVersion(final YangVersion version) {
        return switch (version) {
            case VERSION_1 -> RFC6020.INSTANCE;
            case VERSION_1_1 -> RFC7950.INSTANCE;
        };
    }

    // TODO: teach the only caller about versions, or provide common-enough idioms for its use case
    static @NonNull ArgumentContextUtils rfc6020() {
        return RFC6020.INSTANCE;
    }

    /*
     * NOTE: this method we do not use convenience methods provided by generated parser code, but instead are making
     *       based on the grammar assumptions. While this is more verbose, it cuts out a number of unnecessary code,
     *       such as intermediate List allocation et al.
     */
    final @NonNull String stringFromStringContext(final IRArgument argument, final StatementSourceReference ref) {
        if (argument instanceof final Single single) {
            final var str = single.string();
            if (single.needQuoteCheck()) {
                checkUnquoted(str, ref);
            }
            return single.needUnescape() ? unescape(str, ref) : str;
        } else if (argument instanceof Concatenation concat) {
            return concatStrings(concat.parts(), ref);
        } else {
            throw new VerifyException("Unexpected argument " + argument);
        }
    }

    private @NonNull String concatStrings(final List<? extends Single> parts, final StatementSourceReference ref) {
        final var sb = new StringBuilder();
        for (var part : parts) {
            sb.append(part.needUnescape() ? unescape(part.string(), ref) : part.string());
        }
        return sb.toString();
    }

    /*
     * NOTE: Enforcement and transformation logic done by these methods should logically reside in the lexer and ANTLR
     *       account the for it with lexer modes. We do not want to force a re-lexing phase in the parser just because
     *       we decided to let ANTLR do the work.
     */
    abstract void checkDoubleQuoted(String str, StatementSourceReference ref, int backslash);

    abstract void checkUnquoted(String str, StatementSourceReference ref);

    private @NonNull String unescape(final String str, final StatementSourceReference ref) {
        // Now we need to perform some amount of unescaping. This serves as a pre-check before we dispatch
        // validation and processing (which will reuse the work we have done)
        final int backslash = str.indexOf('\\');
        return backslash == -1 ? str : unescape(ref, str, backslash);
    }

    /*
     * Unescape escaped double quotes, tabs, new line and backslash in the inner string and trim the result.
     */
    private @NonNull String unescape(final StatementSourceReference ref, final String str, final int backslash) {
        checkDoubleQuoted(str, ref, backslash);
        final var sb = new StringBuilder(str.length());
        unescapeBackslash(sb, str, backslash);
        return sb.toString();
    }

    @VisibleForTesting
    static void unescapeBackslash(final StringBuilder sb, final String str, final int backslash) {
        String substring = str;
        int backslashIndex = backslash;
        while (true) {
            int nextIndex = backslashIndex + 1;
            if (backslashIndex != -1 && nextIndex < substring.length()) {
                replaceBackslash(sb, substring, nextIndex);
                substring = substring.substring(nextIndex + 1);
                if (substring.length() > 0) {
                    backslashIndex = substring.indexOf('\\');
                } else {
                    break;
                }
            } else {
                sb.append(substring);
                break;
            }
        }
    }

    private static void replaceBackslash(final StringBuilder sb, final String str, final int nextAfterBackslash) {
        int backslash = nextAfterBackslash - 1;
        sb.append(str, 0, backslash);
        final char c = str.charAt(nextAfterBackslash);
        switch (c) {
            case '\\', '"' -> sb.append(c);
            case 't' -> sb.append('\t');
            case 'n' -> sb.append('\n');
            default -> sb.append(str, backslash, nextAfterBackslash + 1);
        }
    }
}
