/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.antlr.AbstractParserErrorListener;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

@NonNullByDefault
public final class SourceExceptionErrorListener extends AbstractParserErrorListener<SourceException> {
    private final StatementSourceReference ref;

    SourceExceptionErrorListener(final StatementSourceReference ref) {
        this.ref = requireNonNull(ref);
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
        return new SourceExceptionErrorListener(ref).parseImpl(recognizer, parseMethod, ref);
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
        return new SourceExceptionErrorListener(ref).parseImpl(lexer, parser, parseMethod, ref);
    }

    @Override
    protected SourceException createException(final Recognizer<?, ?> recognizer,
            final @Nullable Object offendingSymbol, final int line, final int charPositionInLine,
            final String msg, final @Nullable RecognitionException cause) {
        return new SourceException(ref, cause, "%s at %s:%s", msg, line, charPositionInLine);
    }
}