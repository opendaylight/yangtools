/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.function.Supplier;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Utility class for converting ANTLRErrorListener errors to SourceExceptions. This class is NOT thread-safe.
 *
 * @author Robert Varga
 */
@Beta
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
     * Parse a Recognizer extracting its root item.
     *
     * @param recognizer Recognizer to use
     * @param parseMethod Root item extractor method
     * @param ref Source reference
     * @return Parsed item
     * @throws NullPointerException if any argument is null
     * @throws SourceException if a parser error occurs
     */
    public static <T> T parse(final Recognizer<?, ?> recognizer, final Supplier<T> parseMethod,
            final StatementSourceReference ref) {
        final var listener = new Listener(ref);
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(listener);

        final var ret = parseMethod.get();
        listener.validate();
        return ret;
    }

    /**
     * Use a Lexer/Parser pair extracting the parser's root item.
     *
     * @param lexer lexer to use
     * @param parser parser to use
     * @param parseMethod Root item extractor method
     * @param ref Source reference
     * @return Parsed item
     * @throws NullPointerException if any argument is null
     * @throws SourceException if a parser error occurs
     */
    public static <T> T parse(final Lexer lexer, final Parser parser, final Supplier<T> parseMethod,
            final StatementSourceReference ref) {
        final Listener listener = new Listener(ref);
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final T ret = parseMethod.get();
        listener.validate();
        return ret;
    }
}