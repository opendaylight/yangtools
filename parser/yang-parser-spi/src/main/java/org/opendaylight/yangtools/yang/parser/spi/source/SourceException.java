/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Thrown to indicate error in YANG model source.
 */
public class SourceException extends StatementSourceException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance with the specified message and source. The message will be appended with
     * the source reference.
     *
     * @param message Context message
     * @param sourceRef Statement source
     */
    public SourceException(final @NonNull String message, final @NonNull StatementSourceReference sourceRef) {
        super(sourceRef, message);
    }

    /**
     * Create a new instance with the specified message and source. The message will be appended with
     * the source reference.
     *
     * @param message Context message
     * @param sourceRef Statement source
     * @param cause Underlying cause of this exception
     */
    public SourceException(final @NonNull String message, final @NonNull StatementSourceReference sourceRef,
            final Throwable cause) {
        super(sourceRef, message, cause);
    }

    /**
     * Create a new instance with the specified source and a formatted message. The message will be appended with
     * the source reference.
     *
     * @param source Statement source
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     */
    public SourceException(final @NonNull StatementSourceReference source, final @NonNull String format,
            final Object... args) {
        super(source, format, args);
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
    public SourceException(final @NonNull StatementSourceReference source, final Throwable cause,
            final @NonNull String format, final Object... args) {
        super(source, cause, format, args);
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
    public static void throwIf(final boolean expression, final @NonNull StatementSourceReference source,
            final @NonNull String format, final Object... args) {
        if (expression) {
            throw new SourceException(source, format, args);
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
    public static <T> @NonNull T throwIfNull(final @Nullable T obj, final @NonNull StatementSourceReference source,
            final @NonNull String format, final Object... args) {
        if (obj == null) {
            throw new SourceException(source, format, args);
        }
        return obj;
    }

    /**
     * Throw an instance of this exception if an optional is not present. If it is present, this method will return
     * the unwrapped value.
     *
     * @param opt Optional to be checked
     * @param source Statement source reference
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @return Object unwrapped from the opt optional
     * @throws SourceException if the optional is not present
     */
    public static <T> @NonNull T unwrap(final Optional<T> opt, final @NonNull StatementSourceReference source,
            final @NonNull String format, final Object... args) {
        throwIf(opt.isEmpty(), source, format, args);
        return opt.orElseThrow();
    }

}
