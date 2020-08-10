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
            case YangStatementLexer.COLON:
                return fitsChar22(line, charPositionInLine) ? new ColonToken22(source, line, charPositionInLine)
                        : new ColonToken44(source, line, charPositionInLine);
            case YangStatementLexer.SEMICOLON:
                return fitsChar22(line, charPositionInLine) ? new SemicolonToken22(source, line, charPositionInLine)
                        : new SemicolonToken44(source, line, charPositionInLine);
            case YangStatementLexer.LEFT_BRACE:
                return fitsChar22(line, charPositionInLine) ? new LeftBraceToken22(source, line, charPositionInLine)
                        : new LeftBraceToken44(source, line, charPositionInLine);
            case YangStatementLexer.RIGHT_BRACE:
                return fitsChar22(line, charPositionInLine) ? new RightBraceToken22(source, line, charPositionInLine)
                        : new RightBraceToken44(source, line, charPositionInLine);
            case YangStatementLexer.PLUS:
                return fitsChar22(line, charPositionInLine) ? new PlusToken22(source, line, charPositionInLine)
                        : new PlusToken44(source, line, charPositionInLine);
            default:
                return super.create(source, type, start, stop, line, charPositionInLine);
        }
    }

    private static boolean fitsChar22(final int line, final int charPositionInLine) {
        return line >= 0 && line <= 65535 && charPositionInLine >= 0 && charPositionInLine <= 65535;
    }
}
