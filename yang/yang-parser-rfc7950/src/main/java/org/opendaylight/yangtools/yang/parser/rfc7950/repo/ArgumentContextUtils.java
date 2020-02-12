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
import java.util.regex.Pattern;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Utilities for dealing with YANG statement argument strings, encapsulated in ANTLR grammar's ArgumentContext.
 */
enum ArgumentContextUtils {
    /**
     * YANG 1.0 version of strings, which were not completely clarified in RFC6020.
     */
    RFC6020 {
        @Override
        void checkDoubleQuotedString(final String str, final StatementSourceReference ref) {
            // No-op
        }

        @Override
        void checkUnquotedString(final String str, final StatementSourceReference ref) {
            // No-op
        }
    },
    /**
     * YANG 1.1 version of strings, which were clarified in RFC7950.
     */
    // NOTE: the differences clarified lead to a proper ability to delegate this to ANTLR lexer, but that does not
    //       understand versions and needs to work with both.
    RFC7950 {
        @Override
        void checkDoubleQuotedString(final String str, final StatementSourceReference ref) {
            for (int i = 0; i < str.length() - 1; i++) {
                if (str.charAt(i) == '\\') {
                    switch (str.charAt(i + 1)) {
                        case 'n':
                        case 't':
                        case '\\':
                        case '\"':
                            i++;
                            break;
                        default:
                            throw new SourceException(ref, "YANG 1.1: illegal double quoted string (%s). In double "
                                    + "quoted string the backslash must be followed by one of the following character "
                                    + "[n,t,\",\\], but was '%s'.", str, str.charAt(i + 1));
                    }
                }
            }
        }

        @Override
        void checkUnquotedString(final String str, final StatementSourceReference ref) {
            SourceException.throwIf(ANYQUOTE_MATCHER.matchesAnyOf(str), ref,
                "YANG 1.1: unquoted string (%s) contains illegal characters", str);
        }
    };

    private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.whitespace();
    private static final CharMatcher ANYQUOTE_MATCHER = CharMatcher.anyOf("'\"");
    private static final Pattern ESCAPED_DQUOT = Pattern.compile("\\\"", Pattern.LITERAL);
    private static final Pattern ESCAPED_BACKSLASH = Pattern.compile("\\\\", Pattern.LITERAL);
    private static final Pattern ESCAPED_LF = Pattern.compile("\\n", Pattern.LITERAL);
    private static final Pattern ESCAPED_TAB = Pattern.compile("\\t", Pattern.LITERAL);

    static @NonNull ArgumentContextUtils forVersion(final YangVersion version) {
        switch (version) {
            case VERSION_1:
                return RFC6020;
            case VERSION_1_1:
                return RFC7950;
            default:
                throw new IllegalStateException("Unhandled version " + version);
        }
    }

    /*
     * NOTE: this method we do not use convenience methods provided by generated parser code, but instead are making
     *       based on the grammar assumptions. While this is more verbose, it cuts out a number of unnecessary code,
     *       such as intermediate List allocation et al.
     */
    final @NonNull String stringFromStringContext(final ArgumentContext context, final StatementSourceReference ref) {
        // Get first child, which we fully expect to exist and be a lexer token
        final ParseTree firstChild = context.getChild(0);
        verify(firstChild instanceof TerminalNode, "Unexpected shape of %s", context);
        final TerminalNode firstNode = (TerminalNode) firstChild;
        final int firstType = firstNode.getSymbol().getType();
        switch (firstType) {
            case YangStatementParser.IDENTIFIER:
                // Simple case, there is a simple string, which cannot contain anything that we would need to process.
                return firstNode.getText();
            case YangStatementParser.STRING:
                // Complex case, defer to a separate method
                return concatStrings(context, ref);
            default:
                throw new VerifyException("Unexpected first symbol in " + context);
        }
    }

    private String concatStrings(final ArgumentContext context, final StatementSourceReference ref) {
        /*
         * We have multiple fragments. Just search the tree. This code is equivalent to
         *
         *    context.STRING().forEach(stringNode -> appendString(sb, stringNode, ref))
         *
         * except we minimize allocations which that would do.
         */
        final StringBuilder sb = new StringBuilder();
        for (ParseTree child : context.children) {
            verify(child instanceof TerminalNode, "Unexpected fragment component %s", child);
            final TerminalNode childNode = (TerminalNode) child;
            switch (childNode.getSymbol().getType()) {
                case YangStatementParser.SEP:
                    // Ignore whitespace
                    break;
                case YangStatementParser.PLUS:
                    // Operator, which we are handling by concat
                    break;
                case YangStatementParser.STRING:
                    // a lexer string, could be pretty much anything
                    appendString(sb, childNode, ref);
                    break;
                default:
                    throw new VerifyException("Unexpected symbol in " + childNode);
            }
        }
        return sb.toString();
    }

    private void appendString(final StringBuilder sb, final TerminalNode stringNode,
            final StatementSourceReference ref) {

        final String str = stringNode.getText();
        final char firstChar = str.charAt(0);
        final char lastChar = str.charAt(str.length() - 1);
        // NOTE: Enforcement and transformation logic here should certainly be pushed down to the lexer, as ANTLR can
        //       account the for it with lexer modes. One problem is that lexing here depends on version being lexed,
        //       hence we really would have to re-parse the YANG file after determining its version. We certainly do not
        //       want to do that.
        // FIXME: YANGTOOLS-1079: but since we are performing quoting checks, perhaps at least that part could be lexed?
        if (firstChar == '"' && lastChar == '"') {
            final String innerStr = str.substring(1, str.length() - 1);
            /*
             * Unescape escaped double quotes, tabs, new line and backslash
             * in the inner string and trim the result.
             */
            checkDoubleQuotedString(innerStr, ref);
            sb.append(unescape(trimWhitespace(innerStr, stringNode.getSymbol().getCharPositionInLine())));
        } else if (firstChar == '\'' && lastChar == '\'') {
            /*
             * According to RFC6020 a single quote character cannot occur in
             * a single-quoted string, even when preceded by a backslash.
             */
            sb.append(str, 1, str.length() - 1);
        } else {
            checkUnquotedString(str, ref);
            sb.append(str);
        }
    }

    abstract void checkDoubleQuotedString(String str, StatementSourceReference ref);

    abstract void checkUnquotedString(String str, StatementSourceReference ref);

    private static String unescape(final String str) {
        final int backslash = str.indexOf('\\');
        if (backslash == -1) {
            return str;
        }

        // FIXME: YANGTOOLS-1079: given we the leading backslash, it would be more efficient to walk the string and
        //                        unescape in one go
        return ESCAPED_TAB.matcher(
                    ESCAPED_LF.matcher(
                        ESCAPED_BACKSLASH.matcher(
                            ESCAPED_DQUOT.matcher(str).replaceAll("\\\""))
                        .replaceAll("\\\\"))
                    .replaceAll("\\\n"))
               .replaceAll("\\\t");
    }

    @VisibleForTesting
    static String trimWhitespace(final String str, final int dquot) {
        int brk = str.indexOf('\n');
        if (brk == -1) {
            // No need to trim whitespace
            return str;
        }

        // Okay, we may need to do some trimming, set up a builder and append the first segment
        final int length = str.length();
        final StringBuilder sb = new StringBuilder(length);

        // Append first segment, which needs only tail-trimming
        sb.append(str, 0, trimTrailing(str, 0, brk)).append('\n');

        // With that out of the way, setup our iteration state. The string segment we are looking at is
        // str.substring(start, end), which is guaranteed not to include any line breaks, i.e. end <= brk unless we are
        // at the last segment.
        int start = brk + 1;
        brk = str.indexOf('\n', start);

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
