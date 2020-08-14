/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static com.google.common.base.Verify.verify;

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

        return argument != null ? createWithArgument(keyword, argument, statements, line, column)
                : createWithoutArgument(keyword, statements, line, column);
    }

    private static @NonNull IRStatement createWithArgument(final IRKeyword keyword, final IRArgument argument,
            final ImmutableList<IRStatement> statements, final int line, final int column) {
        switch (statements.size()) {
            case 0:
                if (fits22(line, column)) {
                    return new IRStatementA022(keyword, argument, line, column);
                }
                return fits31(line, column) ? new IRStatementA031(keyword, argument, line, column)
                        : new IRStatementA044(keyword, argument, line, column);
            case 1:
                return new IRStatementA144(keyword, argument, statements.get(0), line, column);
            default:
                return new IRStatementAL44(keyword, argument, statements, line, column);
        }
    }

    private static @NonNull IRStatement createWithoutArgument(final IRKeyword keyword,
            final ImmutableList<IRStatement> statements, final int line, final int column) {
        switch (statements.size()) {
            case 0:
                if (fits22(line, column)) {
                    return new IRStatement22(keyword, line, column);
                }
                return fits31(line, column) ? new IRStatement31(keyword, line, column)
                        : new IRStatement44(keyword, line, column);
            case 1:
                final IRStatement statement = statements.get(0);
                if (fits22(line, column)) {
                    return new IRStatement122(keyword, statement, line, column);
                }
                return fits31(line, column) ? new IRStatement131(keyword, statement, line, column)
                        : new IRStatement144(keyword, statement, line, column);
            default:
                if (fits22(line, column)) {
                    return new IRStatementL22(keyword, statements, line, column);
                }
                return fits31(line, column) ? new IRStatementL31(keyword, statements, line, column)
                        : new IRStatementL44(keyword, statements, line, column);
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
        return strings.computeIfAbsent(tree.getText(), Function.identity());
    }

    private String strOf(final Token token) {
        return strings.computeIfAbsent(token.getText(), Function.identity());
    }

    private static boolean fits22(final int line, final int column) {
        return line >= 0 && line <= 65535 && column >= 0 && column <= 65535;
    }

    private static boolean fits31(final int line, final int column) {
        return line >= 0 && line <= 16777215 && column >= 0 && column <= 255;
    }
}
