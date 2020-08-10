/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import com.google.common.annotations.Beta;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;

@Beta
public final class YangStatementLexerTokenFactory extends CompactTokenFactory {
    public static final @NonNull YangStatementLexerTokenFactory INSTANCE = new YangStatementLexerTokenFactory();

    private YangStatementLexerTokenFactory() {
        // Hidden on purpose
    }

    @Override
    protected Token create(final Pair<TokenSource, CharStream> source, final int type, final int start,
            final int stop, final int line, final int charPositionInLine) {
        switch (type) {
            case YangStatementLexer.LEFT_BRACE:
                return createLeftBrace(source, line, charPositionInLine);
            case YangStatementLexer.RIGHT_BRACE:
                return createRightBrace(source, line, charPositionInLine);
            case YangStatementLexer.COLON:
                return createColon(source, line, charPositionInLine);
            case YangStatementLexer.PLUS:
                return createPlus(source, line, charPositionInLine);
            case YangStatementLexer.SEMICOLON:
                return createSemicolon(source, line, charPositionInLine);
            case YangStatementLexer.SEP:
                // Single-space separator tokens are very common
                final CharStream input = source.b;
                if (input != null && start == stop && start < input.size()
                        && " ".equals(input.getText(Interval.of(start, start)))) {
                    if (fitsChar22(line, charPositionInLine)) {
                        return new SPSepToken22(source, line, charPositionInLine);
                    }
                    if (fitsChar31(line, charPositionInLine)) {
                        return new SPSepToken31(source, line, charPositionInLine);
                    }
                }
                break;
            default:
                // Default
        }
        return super.create(source, type, start, stop, line, charPositionInLine);
    }

    private static Token createLeftBrace(final Pair<TokenSource, CharStream> source, final int line,
            final int charPositionInLine) {
        if (fitsChar22(line, charPositionInLine)) {
            return new LeftBraceToken22(source, line, charPositionInLine);
        }
        return fitsChar31(line, charPositionInLine) ? new LeftBraceToken31(source, line, charPositionInLine)
                : new LeftBraceToken44(source, line, charPositionInLine);
    }

    private static Token createRightBrace(final Pair<TokenSource, CharStream> source, final int line,
            final int charPositionInLine) {
        if (fitsChar22(line, charPositionInLine)) {
            return new RightBraceToken22(source, line, charPositionInLine);
        }
        return fitsChar31(line, charPositionInLine) ? new RightBraceToken31(source, line, charPositionInLine)
                : new RightBraceToken44(source, line, charPositionInLine);
    }

    private static Token createColon(final Pair<TokenSource, CharStream> source, final int line,
            final int charPositionInLine) {
        if (fitsChar22(line, charPositionInLine)) {
            return new ColonToken22(source, line, charPositionInLine);
        }
        return fitsChar31(line, charPositionInLine) ? new ColonToken31(source, line, charPositionInLine)
                : new ColonToken44(source, line, charPositionInLine);
    }

    private static Token createPlus(final Pair<TokenSource, CharStream> source, final int line,
            final int charPositionInLine) {
        if (fitsChar22(line, charPositionInLine)) {
            return new PlusToken22(source, line, charPositionInLine);
        }
        return fitsChar31(line, charPositionInLine) ? new PlusToken31(source, line, charPositionInLine)
                : new PlusToken44(source, line, charPositionInLine);
    }

    private static Token createSemicolon(final Pair<TokenSource, CharStream> source, final int line,
            final int charPositionInLine) {
        if (fitsChar22(line, charPositionInLine)) {
            return new SemicolonToken22(source, line, charPositionInLine);
        }
        return fitsChar31(line, charPositionInLine) ? new SemicolonToken31(source, line, charPositionInLine)
                : new SemicolonToken44(source, line, charPositionInLine);
    }

    private static boolean fitsChar22(final int line, final int charPositionInLine) {
        return line >= 0 && line <= 65535 && charPositionInLine >= 0 && charPositionInLine <= 65535;
    }

    private static boolean fitsChar31(final int line, final int charPositionInLine) {
        return line >= 0 && line <= 16777215 && charPositionInLine >= 0 && charPositionInLine <= 255;
    }
}
