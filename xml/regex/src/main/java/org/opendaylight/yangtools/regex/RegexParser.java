/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import java.util.ArrayList;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.regex.antlr.regexLexer;
import org.opendaylight.yangtools.regex.antlr.regexParser;
import org.opendaylight.yangtools.regex.antlr.regexParser.RootContext;

/**
 * A parser of XSD regular expressions.
 */
final class RegexParser extends BaseErrorListener {
    @NonNullByDefault
    private final ArrayList<RegularExpressionException> exceptions = new ArrayList<>();

    @NonNullByDefault
    static RootContext parseRegularExpression(final String str) throws RegularExpressionException {
        return new RegexParser().parseRegex(str);
    }

    @NonNullByDefault
    private RootContext parseRegex(final String str) throws RegularExpressionException {
        final var lexer = new regexLexer(CharStreams.fromString(str));
        final var parser = new regexParser(new CommonTokenStream(lexer));

        // disconnect from console and hook ourselves
        lexer.removeErrorListeners();
        lexer.addErrorListener(this);
        parser.removeErrorListeners();
        parser.addErrorListener(this);

        final var root = parser.root();

        final var it = exceptions.iterator();
        if (!it.hasNext()) {
            return root;
        }

        final var first = it.next();
        if (!it.hasNext()) {
            // Single exception: just throw it
            throw first;
        }

        final var sb = new StringBuilder().append(first.getMessage());
        it.forEachRemaining(next -> sb.append('\n').append(next.getMessage()));

        throw new RegularExpressionException(first.line(), first.charPositionInLine(), sb.toString(), null);
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException cause) {
        exceptions.add(new RegularExpressionException(line, charPositionInLine, msg, cause));
    }
}
