/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link Exception} signalling a run-time
 * <a href="https://en.wikipedia.org/wiki/Halt_and_Catch_Fire_(computing)">Halt and Catch Fire</a> condition.
 *
 * @since 16.2.0
 * @see VerifyException
 */
@NonNullByDefault
public final class HACFException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with specified message.
     *
     * @param message the message
     */
    public HACFException(final String message) {
        super(requireNonNull(message));
    }

    /**
     * Constructs an exception with specified message and optional cause.
     *
     * @param message the message
     * @param cause the cause, or {@code null}
     */
    public HACFException(final String message, final @Nullable Throwable cause) {
        super(requireNonNull(message), cause);
    }

    /**
     * Constructs an exception with a {@link String#formatted(Object...)} message and optional cause.
     *
     * @param cause the cause, or {@code null}
     * @param format the format string
     * @param args format arguments
     */
    @FormatMethod
    public HACFException(final @Nullable Throwable cause, @FormatString final String format,
            final Object @Nullable ... args) {
        super(format.formatted(args), cause);
    }
}
