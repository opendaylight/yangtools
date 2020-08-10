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
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;

@Beta
public final class CompactTokenFactory implements TokenFactory<Token> {
    public static final @NonNull CompactTokenFactory INSTANCE = new CompactTokenFactory();

    private CompactTokenFactory() {
        // Hidden on purpose
    }

    @Override
    public Token create(final Pair<TokenSource, CharStream> source, final int type, final String text,
            final int channel, final int start, final int stop, final int line, final int charPositionInLine) {
        if (channel != Token.DEFAULT_CHANNEL || text != null) {
            // Non-default channel or text present, defer to common token factory
            return CommonTokenFactory.DEFAULT.create(source, type, text, channel, start, stop, line,
                charPositionInLine);
        }

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
                return create(source, type, start, stop, line, charPositionInLine);
        }
    }

    @Override
    public Token create(final int type, final String text) {
        return new ExplicitTextToken(type, text);
    }

    private static Token create(final Pair<TokenSource, CharStream> source, final int type, final int start,
            final int stop, final int line, final int charPositionInLine) {
        // Can we fit token type into a single byte? This should always be true
        if (type >= Byte.MIN_VALUE && type <= Byte.MAX_VALUE) {
            // Can we fit line in an unsigned short? This is usually be true
            if (line >= 0 && line <= 65535) {
                // Can we fit position in line into an unsigned byte? This is usually true
                if (charPositionInLine >= 0 && charPositionInLine <= 255) {
                    // Can we fit start/stop into an an unsigned short?
                    if (start >= 0 && start <= 65535 && stop >= 0 && stop <= 65535) {
                        return new Token12122(source, type, line, charPositionInLine, start, stop);
                    }
                    return new Token12144(source, type, line, charPositionInLine, start, stop);
                }
            }
        }

        return new Token44444(source, type, line, charPositionInLine, start, stop);
    }

    private static boolean fitsChar22(final int line, final int charPositionInLine) {
        return line >= 0 && line <= 65535 && charPositionInLine >= 0 && charPositionInLine <= 65535;
    }
}
