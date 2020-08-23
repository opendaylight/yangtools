/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.UnquotedStringContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Utilities for dealing with YANG statement argument strings, encapsulated in ANTLR grammar's ArgumentContext.
 */
abstract class ArgumentContextUtils {
    /**
     * YANG 1.0 version of strings, which were not completely clarified in
     * <a href="https://tools.ietf.org/html/rfc6020#section-6.1.3">RFC6020</a>.
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
     * <a href="https://tools.ietf.org/html/rfc7950#section-6.1.3">RFC7950</a>.
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
                    switch (str.charAt(index + 1)) {
                        case 'n':
                        case 't':
                        case '\\':
                        case '\"':
                            index = str.indexOf('\\', index + 2);
                            break;
                        default:
                            throw new SourceException(ref, "YANG 1.1: illegal double quoted string (%s). In double "
                                + "quoted string the backslash must be followed by one of the following character "
                                + "[n,t,\",\\], but was '%s'.", str, str.charAt(index + 1));
                    }
                }
            }
        }

        @Override
        void checkUnquoted(final String str, final StatementSourceReference ref) {
            SourceException.throwIf(ANYQUOTE_MATCHER.matchesAnyOf(str), ref,
                "YANG 1.1: unquoted string (%s) contains illegal characters", str);
        }
    }

    private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.whitespace();

    private ArgumentContextUtils() {
        // Hidden on purpose
    }

    static @NonNull ArgumentContextUtils forVersion(final YangVersion version) {
        switch (version) {
            case VERSION_1:
                return RFC6020.INSTANCE;
            case VERSION_1_1:
                return RFC7950.INSTANCE;
            default:
                throw new IllegalStateException("Unhandled version " + version);
        }
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
    final @NonNull String stringFromStringContext(final ArgumentContext context, final StatementSourceReference ref) {
        // Get first child, which we fully expect to exist and be a lexer token
        final ParseTree firstChild = context.getChild(0);
        if (firstChild instanceof TerminalNode) {
            final Token token = ((TerminalNode) firstChild).getSymbol();
            switch (token.getType()) {
                case YangStatementParser.IDENTIFIER:
                    // Simplest of cases -- it is an IDENTIFIER, hence we do not need to validate anything else and can
                    // just grab the string and run with it.
                    return firstChild.getText();
                case YangStatementParser.DQUOT_START:
                case YangStatementParser.SQUOT_START:
                    // Quoted strings are potentially a pain, deal with them separately
                    return decodeQuoted(context, ref);
                default:
                    throw new VerifyException("Unexpected token " + token);
            }
        }

        verify(firstChild instanceof UnquotedStringContext, "Unexpected shape of %s", context);
        // Simple case, just grab the text, as ANTLR has done all the heavy lifting
        final String str = firstChild.getText();
        checkUnquoted(str, ref);
        return str;
    }

    private @NonNull String decodeQuoted(final ArgumentContext context, final StatementSourceReference ref) {
        if (context.getChildCount() > 3) {
            // Potentially-complex case of string quoting, escaping and concatenation.
            return concatStrings(context, ref);
        }

        // No concatenation needed, special-case
        final ParseTree child = context.getChild(1);
        verify(child instanceof TerminalNode, "Unexpected shape of %s", context);
        final Token token = ((TerminalNode) child).getSymbol();
        switch (token.getType()) {
            case YangStatementParser.DQUOT_END:
            case YangStatementParser.SQUOT_END:
                // We are missing actual body, hence this is an empty string
                return "";
            case YangStatementParser.SQUOT_STRING:
                return token.getText();
            case YangStatementParser.DQUOT_STRING:
                return normalizeDoubleQuoted(token, ref);
            default:
                throw new VerifyException("Unhandled token " + token);
        }
    }

    private String concatStrings(final ArgumentContext context, final StatementSourceReference ref) {
        /*
         * We have multiple fragments. Just search the tree. This code is equivalent to
         *
         *    context.quotedString().forEach(stringNode -> sb.append(unquoteString(stringNode, ref))
         *
         * except we minimize allocations which that would do.
         */
        final StringBuilder sb = new StringBuilder();
        for (ParseTree child : context.children) {
            verify(child instanceof TerminalNode, "Unexpected fragment component %s", child);
            final Token token = ((TerminalNode) child).getSymbol();
            switch (token.getType()) {
                case YangStatementParser.SEP:
                    // Separator, just skip it over
                case YangStatementParser.PLUS:
                    // Operator, which we are handling by concat, skip it over
                case YangStatementParser.DQUOT_START:
                case YangStatementParser.SQUOT_START:
                    // Quote starts, skip them over as they are just markers
                case YangStatementParser.DQUOT_END:
                case YangStatementParser.SQUOT_END:
                    // Quote stops, skip them over because we either already added the content, or would be appending
                    // an empty string
                    break;
                case YangStatementParser.SQUOT_STRING:
                    // Single-quoted string, append it as a literal
                    sb.append(token.getText());
                    break;
                case YangStatementParser.DQUOT_STRING:
                    sb.append(normalizeDoubleQuoted(token, ref));
                    break;
                default:
                    throw new VerifyException("Unexpected token " + token);
            }
        }
        return sb.toString();
    }

    private String normalizeDoubleQuoted(final Token token, final StatementSourceReference ref) {
        // Whitespace normalization happens irrespective of further handling and has no effect on the result. Strictly
        // speaking we should also have the previous token, which would be a DQUOT_START and get the position from it.
        // Seeing as it is a single-character token let's just subtract one from this token to achieve the same result.
        final String stripped = trimWhitespace(token.getText(), token.getCharPositionInLine() - 1);

        // Now we need to perform some amount of unescaping. This serves as a pre-check before we dispatch
        // validation and processing (which will reuse the work we have done)
        final int backslash = stripped.indexOf('\\');
        return backslash == -1 ? stripped : unescape(ref, stripped, backslash);
    }

    /*
     * NOTE: Enforcement and transformation logic done by these methods should logically reside in the lexer and ANTLR
     *       account the for it with lexer modes. We do not want to force a re-lexing phase in the parser just because
     *       we decided to let ANTLR do the work.
     */
    abstract void checkDoubleQuoted(String str, StatementSourceReference ref, int backslash);

    abstract void checkUnquoted(String str, StatementSourceReference ref);

    /*
     * Unescape escaped double quotes, tabs, new line and backslash in the inner string and trim the result.
     */
    private String unescape(final StatementSourceReference ref, final String str, final int backslash) {
        checkDoubleQuoted(str, ref, backslash);
        StringBuilder sb = new StringBuilder(str.length());
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
            case '\\':
            case '"':
                sb.append(c);
                break;
            case 't':
                sb.append('\t');
                break;
            case 'n':
                sb.append('\n');
                break;
            default:
                sb.append(str, backslash, nextAfterBackslash + 1);
        }
    }

    @VisibleForTesting
    static String trimWhitespace(final String str, final int dquot) {
        final int firstBrk = str.indexOf('\n');
        if (firstBrk == -1) {
            return str;
        }

        // Okay, we may need to do some trimming, set up a builder and append the first segment
        final int length = str.length();
        final StringBuilder sb = new StringBuilder(length);

        // Append first segment, which needs only tail-trimming
        sb.append(str, 0, trimTrailing(str, 0, firstBrk)).append('\n');

        // With that out of the way, setup our iteration state. The string segment we are looking at is
        // str.substring(start, end), which is guaranteed not to include any line breaks, i.e. end <= brk unless we are
        // at the last segment.
        int start = firstBrk + 1;
        int brk = str.indexOf('\n', start);

        // Loop over inner strings
        while (brk != -1) {
            trimLeadingAndAppend(sb, dquot, str, start, trimTrailing(str, start, brk)).append('\n');
            start = brk + 1;
            brk = str.indexOf('\n', start);
        }

        return trimLeadingAndAppend(sb, dquot, str, start, length).toString();
    }

    private static StringBuilder trimLeadingAndAppend(final StringBuilder sb, final int dquot, final String str,
            final int start, final int end) {
        int offset = start;
        int pos = 0;

        while (pos <= dquot) {
            if (offset == end) {
                // We ran out of data, nothing to append
                return sb;
            }

            final char ch = str.charAt(offset);
            if (ch == '\t') {
                // tabs are to be treated as 8 spaces
                pos += 8;
            } else if (WHITESPACE_MATCHER.matches(ch)) {
                pos++;
            } else {
                break;
            }

            offset++;
        }

        // We have expanded beyond double quotes, push equivalent spaces
        while (pos - 1 > dquot) {
            sb.append(' ');
            pos--;
        }

        return sb.append(str, offset, end);
    }

    private static int trimTrailing(final String str, final int start, final int end) {
        int ret = end;
        while (ret > start) {
            final int prev = ret - 1;
            if (!WHITESPACE_MATCHER.matches(str.charAt(prev))) {
                break;
            }
            ret = prev;
        }
        return ret;
    }
}
