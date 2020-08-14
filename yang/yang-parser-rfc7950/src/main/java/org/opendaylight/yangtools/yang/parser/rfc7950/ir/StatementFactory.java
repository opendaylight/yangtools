/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.KeywordContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.QuotedStringContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.UnquotedStringContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Concatenation;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.DoubleQuoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.SingleQuoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Unquoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Unqualified;

final class StatementFactory {
    private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.whitespace();

    private final Map<String, DoubleQuoted> dquotArguments = new HashMap<>();
    private final Map<String, SingleQuoted> squotArguments = new HashMap<>();
    private final Map<String, Unquoted> uquotArguments = new HashMap<>();
    private final Map<String, IRKeyword> keywords = new HashMap<>();
    private final Map<String, String> strings = new HashMap<>();

    @NonNull IRStatement createStatement(final StatementContext stmt) {
        final ParseTree firstChild = stmt.getChild(0);
        verify(firstChild instanceof KeywordContext, "Unexpected shape of %s", stmt);

        final ParseTree keywordStart = firstChild.getChild(0);
        verify(keywordStart instanceof TerminalNode, "Unexpected keyword start %s", keywordStart);
        final Token keywordToken = ((TerminalNode) keywordStart).getSymbol();

        final IRKeyword keyword;
        switch (firstChild.getChildCount()) {
            case 1:
                keyword = keywords.computeIfAbsent(strOf(keywordToken), Unqualified::new);
                break;
            case 3:
                keyword = new Qualified(strOf(keywordToken), strOf(firstChild.getChild(2)));
                break;
            default:
                throw new VerifyException("Unexpected keyword " + firstChild);
        }

        final IRArgument argument = createArgument(stmt);
        final ImmutableList<IRStatement> statements = createStatements(stmt);
        final int line = keywordToken.getLine();
        final int column = keywordToken.getCharPositionInLine();

        switch (statements.size()) {
            case 0:
                if (fits22(line, column)) {
                    return new IRStatement022(keyword, argument, line, column);
                }
                return fits31(line, column) ? new IRStatement031(keyword, argument, line, column)
                        : new IRStatement044(keyword, argument, line, column);
            case 1:
                return new IRStatement144(keyword, argument, statements.get(0), line, column);
            default:
                return new IRStatementL44(keyword, argument, statements, line, column);
        }
    }

    private IRArgument createArgument(final StatementContext stmt) {
        final ArgumentContext argCtx = stmt.argument();
        if (argCtx == null) {
            return null;
        }
        if (argCtx.getChildCount() == 1) {
            final ParseTree child = argCtx.getChild(0);
            if (child instanceof UnquotedStringContext) {
                return uquotArguments.computeIfAbsent(strOf(child), Unquoted::new);
            }

            verify(child instanceof QuotedStringContext, "Unexpected child %s", child);
            return createArgument((QuotedStringContext) child);
        }

        return new Concatenation(argCtx.quotedString().stream().map(this::createArgument)
            .collect(ImmutableList.toImmutableList()));
    }

    private IRArgument createArgument(final QuotedStringContext argument) {
        final ParseTree literal = argument.getChild(1);
        verify(literal instanceof TerminalNode, "Unexpected literal %s", literal);
        final Token token = ((TerminalNode) literal).getSymbol();
        switch (token.getType()) {
            case YangStatementParser.DQUOT_END:
                return dquotArguments.computeIfAbsent("", DoubleQuoted::new);
            case YangStatementParser.DQUOT_STRING:
                // Whitespace normalization happens irrespective of further handling and has no effect on the result
                final String str = intern(trimWhitespace(token.getText(), token.getCharPositionInLine() - 1));
                return dquotArguments.computeIfAbsent(strOf(token), DoubleQuoted::new);
            case YangStatementParser.SQUOT_END:
                return squotArguments.computeIfAbsent("", SingleQuoted::new);
            case YangStatementParser.SQUOT_STRING:
                return squotArguments.computeIfAbsent(strOf(token), SingleQuoted::new);
            default:
                throw new VerifyException("Unexpected token " + token);
        }
    }

    private ImmutableList<IRStatement> createStatements(final StatementContext stmt) {
        final List<StatementContext> statements = stmt.statement();
        return statements.isEmpty() ? ImmutableList.of()
                : statements.stream().map(this::createStatement).collect(ImmutableList.toImmutableList());
    }

    private String strOf(final ParseTree tree) {
        return intern(tree.getText());
    }

    private String strOf(final Token token) {
        return intern(token.getText());
    }

    private String intern(final String str) {
        return strings.computeIfAbsent(str, Function.identity());
    }

    private static boolean fits22(final int line, final int column) {
        return line >= 0 && line <= 65535 && column >= 0 && column <= 65535;
    }

    private static boolean fits31(final int line, final int column) {
        return line >= 0 && line <= 16777215 && column >= 0 && column <= 255;
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
