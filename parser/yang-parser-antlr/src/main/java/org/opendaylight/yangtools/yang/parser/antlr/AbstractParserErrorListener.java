/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base type for converting {@link ANTLRErrorListener} errors to {@link Exception}s. This class is NOT
 * thread-safe, nor are its subclasses expected to be thread-safe.
 *
 * @param <E> Reported exception type
 */
@NonNullByDefault
public abstract class AbstractParserErrorListener<E extends Exception> extends BaseErrorListener implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractParserErrorListener.class);

    private final List<E> exceptions = new ArrayList<>();

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
            final int line, final int charPositionInLine, final @Nullable String msg,
            final @Nullable RecognitionException e) {
        LOG.debug("Syntax error at {}:{}: {}", line, charPositionInLine, msg, e);
        exceptions.add(verifyNotNull(createException(verifyNotNull(recognizer), offendingSymbol, line,
            charPositionInLine, verifyNotNull(msg), e)));
    }

    protected abstract E createException(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
            int charPositionInLine, String msg, @Nullable RecognitionException cause);

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
    protected final <T> T parseImpl(final Recognizer<?, ?> recognizer, final Supplier<T> parseMethod,
            final StatementSourceReference ref) throws E {
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(this);

        final var ret = parseMethod.get();
        validate();
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
    protected final <T> T parseImpl(final Lexer lexer, final Parser parser, final Supplier<T> parseMethod,
            final StatementSourceReference ref) throws E {
        lexer.removeErrorListeners();
        lexer.addErrorListener(this);
        parser.removeErrorListeners();
        parser.addErrorListener(this);

        final T ret = parseMethod.get();
        validate();
        return ret;
    }

    private void validate() throws E {
        if (!exceptions.isEmpty()) {
            final var it = exceptions.iterator();
            final var exception = it.next();
            it.forEachRemaining(exception::addSuppressed);
            throw exception;
        }
    }
}
