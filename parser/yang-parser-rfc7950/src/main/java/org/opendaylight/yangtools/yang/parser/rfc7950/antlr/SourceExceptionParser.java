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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Utility class for converting ANTLRErrorListener errors to {@link SourceException}s.
 */
@NonNullByDefault
public final class SourceExceptionParser {
    private static final class Listener extends AbstractParserErrorListener<SourceException> {
        private final StatementSourceReference ref;

        private Listener(final StatementSourceReference ref) {
            this.ref = requireNonNull(ref);
        }

        @Override
        protected SourceException createException(final Recognizer<?, ?> recognizer,
                final @Nullable Object offendingSymbol, final int line, final int charPositionInLine,
                final String msg, final @Nullable RecognitionException cause) {
            return new SourceException(ref, cause, "%s at %s:%s", msg, line, charPositionInLine);
        }
    }

    private SourceExceptionParser() {
        // Hidden on purpose
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
    public static <L extends Lexer, P extends Parser, @NonNull R> R parseString(final Function<CharStream, L> lexerCtor,
            final Function<TokenStream, P> parserCtor, Function<P, R> parseMethod, final StatementSourceReference ref,
            final String str) {
        final var listener = new Listener(ref);
        final var lexer = lexerCtor.apply(requireNonNull(CharStreams.fromString(str)));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        final var parser = parserCtor.apply(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final var ret = parseMethod.apply(parser);
        listener.validate();
        return SourceException.throwIfNull(ret, ref, "No root extracted");
    }
}