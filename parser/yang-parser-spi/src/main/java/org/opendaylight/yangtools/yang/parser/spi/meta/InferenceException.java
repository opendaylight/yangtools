/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * A {@link StatementSourceException} indicating an inference problem, e.g. a problem with how statements interact with
 * each other.
 */
public final class InferenceException extends StatementSourceException {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    public InferenceException(final @NonNull String message, final @NonNull StatementSourceReference source) {
        super(source, message);
    }

    public InferenceException(final @NonNull String message, final @NonNull StatementSourceReference source,
            final Throwable cause) {
        super(source, message, cause);
    }

    public InferenceException(final @NonNull StatementSourceReference source, final @NonNull String format,
            final Object... args) {
        super(source, format, args);
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
    public static void throwIf(final boolean expression, final @NonNull StatementSourceReference source,
            final @NonNull String format, final Object... args) {
        if (expression) {
            throw new InferenceException(source, format, args);
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
     * @throws InferenceException if object is null
     */
    public static <T> @NonNull T throwIfNull(final @Nullable T obj, final @NonNull StatementSourceReference source,
            final @NonNull String format, final Object... args) {
        if (obj == null) {
            throw new InferenceException(source, format, args);
        }
        return obj;
    }
}
