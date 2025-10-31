/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting ANTLRErrorListener errors to {@link SourceException}s.
 */
public final class SourceExceptionParser extends BaseErrorListener {
    private static final Logger LOG = LoggerFactory.getLogger(SourceExceptionParser.class);

    private final @NonNull StatementSourceReference ref;

    private SourceException exception;

    @NonNullByDefault
    private SourceExceptionParser(final StatementSourceReference ref) {
        this.ref = requireNonNull(ref);
    }

    /**
     * Parse some content using a {@link Lexer} and a {@link Parser}, extracting a the parser's root.
     *
     * @param <L> Lexer type
     * @param <P> Parser type
     * @param <R> Parser root type
     * @param lexerCtor Lexec constructor, typically {@code L::new}
     * @param parserCtor Parser constructor, typically {@code P::new}
     * @param parseMethod Parser's method extracting the root rule
     * @param ref reference to the statement
     * @param str the String to parse
     * @return the root
     * @throws NullPointerException if any argument is {@code null}
     * @throws SourceException if a parser error occurs
     */
    @NonNullByDefault
    public static <L extends Lexer, P extends Parser, @NonNull R> R parseString(final Function<CharStream, L> lexerCtor,
            final Function<TokenStream, P> parserCtor, Function<P, R> parseMethod, final StatementSourceReference ref,
            final String str) {
        return new SourceExceptionParser(ref)
            .parse(lexerCtor, parserCtor, parseMethod, requireNonNull(CharStreams.fromString(str)));
    }

    @NonNullByDefault
    private <L extends Lexer, P extends Parser, @NonNull R> R parse(final Function<CharStream, L> lexerCtor,
            final Function<TokenStream, P> parserCtor, Function<P, R> parseMethod, final CharStream stream) {
        final var lexer = lexerCtor.apply(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(this);

        final var parser = parserCtor.apply(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(this);

        final var ret = parseMethod.apply(parser);
        if (exception != null) {
            throw exception;
        }
        return SourceException.throwIfNull(ret, ref, "No root extracted");
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException cause) {
        LOG.debug("Syntax error at {}:{}: {}", line, charPositionInLine, msg, cause);
        final var ex = new SourceException(ref, cause, "%s at %s:%s", msg, line, charPositionInLine);
        if (exception != null) {
            exception.addSuppressed(ex);
        } else {
            exception = ex;
        }
    }
}