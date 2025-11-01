/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.FileContext;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.KeywordContext;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.UnquotedStringContext;

final class IRSupport {
    private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.whitespace();

    private final HashMap<String, Single> dquotArguments = new HashMap<>();
    private final HashMap<String, Single> squotArguments = new HashMap<>();
    private final HashMap<String, Single> uquotArguments = new HashMap<>();
    private final HashMap<String, Single> idenArguments = new HashMap<>();
    private final HashMap<String, Unqualified> uqualKeywords = new HashMap<>();
    private final HashMap<Entry<String, String>, Qualified> qualKeywords = new HashMap<>();
    private final HashMap<String, String> strings = new HashMap<>();

    private IRSupport() {
        // Hidden on purpose
    }

    /**
     * Create an {@link IRStatement} from a parsed {@link FileContext}.
     *
     * @param file ANTLR file context
     * @return A new IRStatement
     * @throws NullPointerException if {@code file} is null or it does not contain a root statement
     */
    static @NonNull IRStatement createStatement(final FileContext file) {
        return new IRSupport().statementOf(file.statement());
    }

    private @NonNull IRStatement statementOf(final StatementContext stmt) {
        if (!(stmt.getChild(0) instanceof KeywordContext firstChild)) {
            throw new VerifyException("Unexpected shape of " + stmt);
        }

        final var keywordToken = verifyTerminal("keyword start", firstChild.getChild(0));
        final var keyword = switch (firstChild.getChildCount()) {
            case 1 -> uqualKeywords.computeIfAbsent(strOf(keywordToken), Unqualified::of);
            case 3 -> qualKeywords.computeIfAbsent(Map.entry(strOf(keywordToken), strOf(firstChild.getChild(2))),
                entry -> Qualified.of(entry.getKey(), entry.getValue()));
            default -> throw new VerifyException("Unexpected keyword " + firstChild);
        };
        final var argument = createArgument(stmt);
        final var statements = createStatements(stmt);
        final int line = keywordToken.getLine();
        final int column = keywordToken.getCharPositionInLine();

        return IRStatement.of(keyword, argument, line, column, statements);
    }

    private IRArgument createArgument(final StatementContext stmt) {
        final var argument = stmt.argument();
        if (argument == null) {
            return null;
        }
        return switch (argument.getChildCount()) {
            case 0 -> throw new VerifyException("Unexpected shape of " + argument);
            case 1 -> createSimple(argument);
            case 2 -> createQuoted(argument);
            default -> createConcatenation(argument);
        };
    }

    private IRArgument createConcatenation(final ArgumentContext argument) {
        final var parts = new ArrayList<Single>();

        for (var child : argument.children) {
            final var token = verifyTerminal("argument component", child);
            switch (token.getType()) {
                case YangStatementParser.SEP:
                    // Separator, just skip it over
                case YangStatementParser.PLUS:
                    // Operator, which we are handling by concat, skip it over
                case YangStatementParser.DQUOT_END:
                case YangStatementParser.SQUOT_END:
                    // Quote stops, skip them over because we either already added the content, or would be appending
                    // an empty string
                    break;
                case YangStatementParser.SQUOT_STRING:
                    parts.add(createSingleQuoted(token));
                    break;
                case YangStatementParser.DQUOT_STRING:
                    parts.add(createDoubleQuoted(token));
                    break;
                default:
                    throw unexpectedToken(token);
            }
        }

        return IRArgument.of(parts);
    }

    private Single createQuoted(final ArgumentContext argument) {
        final var token = verifyTerminal("liternal", argument.getChild(0));
        return switch (token.getType()) {
            case YangStatementParser.DQUOT_STRING -> createDoubleQuoted(token);
            case YangStatementParser.SQUOT_STRING -> createSingleQuoted(token);
            default -> throw unexpectedToken(token);
        };
    }

    private Single createDoubleQuoted(final Token token) {
        // Whitespace normalization happens irrespective of further handling and has no effect on the result
        final String str = intern(trimWhitespace(token.getText(), token.getCharPositionInLine() - 1));

        // TODO: turn this into a single-quoted literal if a backslash is not present. Doing so allows the
        //       argument to be treated as a literal. See IRArgument.Single#needUnescape() for more context.
        //       This may look unimportant, but there are scenarios where we process the same AST multiple times
        //       and remembering this detail saves a string scan.

        return dquotArguments.computeIfAbsent(str, IRArgument::doubleQuoted);
    }

    private Single createSimple(final ArgumentContext argument) {
        return switch (argument.getChild(0)) {
            case TerminalNode terminal -> {
                final var token = terminal.getSymbol();
                yield switch (token.getType()) {
                    // This is as simple as it gets: we are dealing with an identifier here.
                    case YangStatementParser.IDENTIFIER -> idenArguments.computeIfAbsent(strOf(token),
                        IRArgument::identifier);
                    // This is an empty string, the difference between double and single quotes does not exist. Single
                    // quotes have more stringent semantics, hence use those.
                    case YangStatementParser.DQUOT_END, YangStatementParser.SQUOT_END -> IRArgument.empty();
                    default -> throw unexpectedToken(token);
                };
            }
            case UnquotedStringContext unquoted -> {
                // TODO: check non-presence of quotes and create a different subclass, so that ends up treated as if it
                //       was single-quoted, i.e. bypass the check implied by IRArgument.Single#needQuoteCheck().
                yield uquotArguments.computeIfAbsent(strOf(unquoted), IRArgument::unquoted);
            }
            default -> throw new VerifyException("Unexpected shape of " + argument);
        };
    }

    private Single createSingleQuoted(final Token token) {
        return squotArguments.computeIfAbsent(strOf(token), IRArgument::singleQuoted);
    }

    private ImmutableList<IRStatement> createStatements(final StatementContext stmt) {
        final var statements = stmt.statement();
        return statements.isEmpty() ? ImmutableList.of()
            : statements.stream().map(this::statementOf).collect(ImmutableList.toImmutableList());
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

    @VisibleForTesting
    static String trimWhitespace(final String str, final int dquot) {
        final int firstBrk = str.indexOf('\n');
        if (firstBrk == -1) {
            return str;
        }

        // Okay, we may need to do some trimming, set up a builder and append the first segment
        final int length = str.length();
        final var sb = new StringBuilder(length);

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

    private static VerifyException unexpectedToken(final Token token) {
        return new VerifyException("Unexpected token " + token);
    }

    private static Token verifyTerminal(final String what, final ParseTree tree) {
        if (tree instanceof TerminalNode terminal) {
            return terminal.getSymbol();
        }
        throw new VerifyException("Unexpected " + what + " "+ tree);
    }
}
