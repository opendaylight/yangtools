/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

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
    private static final CharMatcher ANYQUOTE_MATCHER = CharMatcher.anyOf("'\"");
    private static final Pattern ESCAPED_DQUOT = Pattern.compile("\\\"", Pattern.LITERAL);
    private static final Pattern ESCAPED_BACKSLASH = Pattern.compile("\\\\", Pattern.LITERAL);
    private static final Pattern ESCAPED_LF = Pattern.compile("\\n", Pattern.LITERAL);
    private static final Pattern ESCAPED_TAB = Pattern.compile("\\t", Pattern.LITERAL);

    private ArgumentContextUtils() {
        throw new UnsupportedOperationException();
    }

    static String stringFromStringContext(final ArgumentContext context, final StatementSourceReference ref) {
        return stringFromStringContext(context, YangVersion.VERSION_1, ref);
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
                            ESCAPED_DQUOT.matcher(innerStr).replaceAll("\\\""))
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
}
