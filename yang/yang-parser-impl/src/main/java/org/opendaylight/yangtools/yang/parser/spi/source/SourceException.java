/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Thrown to indicate error in YANG model source.
 */
public class SourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final StatementSourceReference sourceRef;
    private final SourceIdentifier sourceId;

    /**
     * Create a new instance with the specified message and source. The message will be appended with
     * the source reference.
     *
     * @param message Context message
     * @param source Statement source
     */
    public SourceException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final SourceIdentifier id) {
        super(createMessage(message, source));
        sourceRef = source;
        sourceId = id;
    }

    /**
     * Create a new instance with the specified message and source. The message will be appended with
     * the source reference.
     *
     * @param message Context message
     * @param source Statement source
     * @param cause Underlying cause of this exception
     */
    public SourceException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final SourceIdentifier id, final Throwable cause) {
        super(createMessage(message, source), cause);
        sourceRef = source;
        sourceId = id;
    }

    /**
     * Create a new instance with the specified source and a formatted message. The message will be appended with
     * the source reference.
     *
     * @param source Statement source
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     */
    public SourceException(@Nonnull final StatementSourceReference source, final SourceIdentifier id,
            @Nonnull final String format, final Object... args) {
        this(String.format(format, args), source, id);
    }

    /**
     * Create a new instance with the specified source and a formatted message. The message will be appended with
     * the source reference.
     *
     * @param source Statement source
     * @param cause Underlying cause of this exception
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     */
    public SourceException(@Nonnull final StatementSourceReference source, final SourceIdentifier id,
            final Throwable cause, @Nonnull final String format, final Object... args) {
        this(String.format(format, args), source, id, cause);
    }

    /**
     * Return the reference to the source which caused this exception.
     *
     * @return Source reference
     */
    public @Nonnull StatementSourceReference getSourceReference() {
        return sourceRef;
    }

    public SourceIdentifier getSourceIdentifier() {
        return sourceId;
    }

    /**
     * Throw an instance of this exception if an expression evaluates to true. If the expression evaluates to false,
     * this method does nothing.
     *
     * @param expression Expression to be evaluated
     * @param source Statement source reference
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @throws SourceException if the expression evaluates to true.
     */
    public static void throwIf(final boolean expression, @Nonnull final StatementSourceReference source,
            final SourceIdentifier id, @Nonnull final String format, final Object... args) {
        if (expression) {
            throw new SourceException(source, id, format, args);
        }
    }

    /**
     * Throw an instance of this exception if an object is null. If the object is non-null, it will
     * be returned as the result of this method.
     *
     * @param obj Object reference to be checked
     * @param source Statement source reference
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @return Object if it is not null
     * @throws SourceException if object is null
     */
    @Nonnull public static <T> T throwIfNull(final T obj, @Nonnull final StatementSourceReference source,
            final SourceIdentifier id, @Nonnull final String format, final Object... args) {
        throwIf(obj == null, source, id, format, args);
        return obj;
    }

    private static String createMessage(@Nonnull final String message, @Nonnull final StatementSourceReference source) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(source);

        return message + " [at " + source + ']';
    }
}
