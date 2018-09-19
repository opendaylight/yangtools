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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class ArgumentContextUtils {
    private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.whitespace();
    private static final CharMatcher ANYQUOTE_MATCHER = CharMatcher.anyOf("'\"");
    private static final Pattern ESCAPED_DQUOT = Pattern.compile("\\\"", Pattern.LITERAL);
    private static final Pattern ESCAPED_BACKSLASH = Pattern.compile("\\\\", Pattern.LITERAL);
    private static final Pattern ESCAPED_LF = Pattern.compile("\\n", Pattern.LITERAL);
    private static final Pattern ESCAPED_TAB = Pattern.compile("\\t", Pattern.LITERAL);

    private ArgumentContextUtils() {
        throw new UnsupportedOperationException();
    }

    static String stringFromStringContext(final ArgumentContext context, final YangVersion yangVersion,
            final StatementSourceReference ref) {
        final StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.isEmpty()) {
            strings = Collections.singletonList(context.IDENTIFIER());
        }
        for (final TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            final char firstChar = str.charAt(0);
            final char lastChar = str.charAt(str.length() - 1);
            if (firstChar == '"' && lastChar == '"') {
                final String innerStr = str.substring(1, str.length() - 1);
                /*
                 * Unescape escaped double quotes, tabs, new line and backslash
                 * in the inner string and trim the result.
                 */
                checkDoubleQuotedString(innerStr, yangVersion, ref);

                sb.append(ESCAPED_TAB.matcher(
                    ESCAPED_LF.matcher(
                        ESCAPED_BACKSLASH.matcher(
                            ESCAPED_DQUOT.matcher(
                                trimWhitespace(innerStr, stringNode.getSymbol().getCharPositionInLine()))
                            .replaceAll("\\\""))
                        .replaceAll("\\\\"))
                    .replaceAll("\\\n"))
                    .replaceAll("\\\t"));
            } else if (firstChar == '\'' && lastChar == '\'') {
                /*
                 * According to RFC6020 a single quote character cannot occur in
                 * a single-quoted string, even when preceded by a backslash.
                 */
                sb.append(str.substring(1, str.length() - 1));
            } else {
                checkUnquotedString(str, yangVersion, ref);
                sb.append(str);
            }
        }
        return sb.toString();
    }

    private static void checkUnquotedString(final String str, final YangVersion yangVersion,
            final StatementSourceReference ref) {
        if (yangVersion == YangVersion.VERSION_1_1) {
            SourceException.throwIf(ANYQUOTE_MATCHER.matchesAnyOf(str), ref,
                "YANG 1.1: unquoted string (%s) contains illegal characters", str);
        }
    }

    private static void checkDoubleQuotedString(final String str, final YangVersion yangVersion,
            final StatementSourceReference ref) {
        if (yangVersion == YangVersion.VERSION_1_1) {
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
