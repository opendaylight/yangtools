/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import java.text.ParseException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * String escaping semantics.
 *
 * @since 14.0.22
 */
public enum StringEscaping {
    /**
     * YANG 1.0 version of string escaping, which was not completely clarified in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.1.3">RFC6020</a>.
     */
    RFC6020 {
        @Override
        void checkDoubleQuoted(final String str, final int backslash) {
            // No-op
        }

        @Override
        void checkUnquoted(final String str) {
            // No-op
        }
    },

    /**
     * YANG 1.1 version of string escaping, which was clarified in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-6.1.3">RFC7950</a>.
     */
    RFC7950 {
        private static final CharMatcher ANYQUOTE_MATCHER = CharMatcher.anyOf("'\"");

        @Override
        void checkDoubleQuoted(final String str, final int backslash) throws ParseException {
            if (backslash < str.length() - 1) {
                int index = backslash;
                while (index != -1) {
                    final var escape = str.charAt(index + 1);
                    index = switch (escape) {
                        case 'n', 't', '\\', '\"' -> str.indexOf('\\', index + 2);
                        default -> throw new ParseException(String.format("""
                            YANG 1.1: illegal double quoted string (%s). In double quoted string the backslash must be \
                            followed by one of the following character [n,t,",\\], but was '%s'.""", str, escape),
                            index);
                    };
                }
            }
        }

        @Override
        void checkUnquoted(final String str) throws ParseException {
            final var index = ANYQUOTE_MATCHER.indexIn(str);
            if (index != -1) {
                throw new ParseException("YANG 1.1: unquoted string (" + str + ") contains illegal characters", index);
            }
        }
    };

    /*
     * NOTE: Enforcement and transformation logic done by these methods should logically reside in the lexer and ANTLR
     *       account the for it with lexer modes. We do not want to force a re-lexing phase in the parser just because
     *       we decided to let ANTLR do the work.
     */
    abstract void checkDoubleQuoted(String str, int backslash) throws ParseException;

    abstract void checkUnquoted(String str) throws ParseException;

    final @NonNull String unescape(final String str) throws ParseException {
        // Now we need to perform some amount of unescaping. This serves as a pre-check before we dispatch
        // validation and processing (which will reuse the work we have done)
        final int backslash = str.indexOf('\\');
        return backslash == -1 ? str : unescape(str, backslash);
    }

    /*
     * Unescape escaped double quotes, tabs, new line and backslash in the inner string and trim the result.
     */
    private @NonNull String unescape(final String str, final int backslash) throws ParseException {
        checkDoubleQuoted(str, backslash);
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
            if (backslashIndex == -1 || nextIndex >= substring.length()) {
                sb.append(substring);
                break;
            }
            replaceBackslash(sb, substring, nextIndex);
            substring = substring.substring(nextIndex + 1);
            if (substring.length() <= 0) {
                break;
            }
            backslashIndex = substring.indexOf('\\');
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
