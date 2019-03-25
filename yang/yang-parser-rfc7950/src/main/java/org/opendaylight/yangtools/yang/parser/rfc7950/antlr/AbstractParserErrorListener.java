/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base type for converting ANTLRErrorListener errors to Exceptions. This class is NOT thread-safe, nor are
 * its subclasses expected to be thread-safe.
 *
 * @param <E> Reported exception type
 * @author Robert Varga
 */
@Beta
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

    public final void validate() throws E {
        if (!exceptions.isEmpty()) {
            final Iterator<E> it = exceptions.iterator();
            final E exception = it.next();
            it.forEachRemaining(exception::addSuppressed);
            throw exception;
        }
    }

    protected abstract E createException(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
            int charPositionInLine, String msg, @Nullable RecognitionException cause);
}
