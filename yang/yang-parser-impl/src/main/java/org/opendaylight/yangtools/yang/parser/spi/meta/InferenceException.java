/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Thrown when there was inference error
 */
public class InferenceException extends SourceException {
    private static final long serialVersionUID = 1L;

    public InferenceException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final SourceIdentifier sourceId, final Throwable cause) {
        super(message, source, sourceId, cause);
    }

    public InferenceException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final SourceIdentifier sourceId) {
        super(message, source, sourceId);
    }

    public InferenceException(@Nonnull final StatementSourceReference source, final SourceIdentifier sourceId,
            @Nonnull final String format, final Object... args) {
        this(String.format(format, args), source, sourceId);
    }

    /**
     * Throw an instance of this exception if an expression evaluates to true. If the expression evaluates to false,
     * this method does nothing.
     *
     * @param expression Expression to be evaluated
     * @param source Statement source reference
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @throws InferenceException if the expression evaluates to true.
     */
    public static void throwIf(final boolean expression, @Nonnull final StatementSourceReference source,
            final SourceIdentifier sourceId, @Nonnull final String format, final Object... args) {
        if (expression) {
            throw new InferenceException(source, sourceId, format, args);
        }
    }
}
