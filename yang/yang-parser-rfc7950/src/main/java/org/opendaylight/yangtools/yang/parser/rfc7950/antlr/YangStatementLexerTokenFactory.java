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
                return CharToken.LEFT_BRACE;
            case YangStatementLexer.RIGHT_BRACE:
                return CharToken.RIGHT_BRACE;
            case YangStatementLexer.COLON:
                return CharToken.COLON;
            case YangStatementLexer.PLUS:
                return CharToken.PLUS;
            case YangStatementLexer.SEMICOLON:
                return CharToken.SEMICOLON;
            case YangStatementLexer.SEP:
                // Single-space separator tokens are very common
                final CharStream input = source.b;
                if (input != null && start == stop && start < input.size()
                        && " ".equals(input.getText(Interval.of(start, start)))) {
                    return CharToken.SEP_SP;
                }
                break;
            default:
                // Default
        }
        return super.create(source, type, start, stop, line, charPositionInLine);
    }
}
