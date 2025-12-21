/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link ArgumentParserException} indicating a problem with binding an argument.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public non-sealed class ArgumentBindingException extends ArgumentParserException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construct an instance with specified {@code message} and {@code position} and an optional cause.
     *
     * @param message the message
     * @param position the error offset, must be non-negative
     * @param cause the cause, {@code null} if not present
     */
    public ArgumentBindingException(final String message, final int position, final @Nullable Exception cause) {
        super(message, position, cause);
    }

    /**
     * Construct an instance with specified {@code message} and {@code position}.
     *
     * @param message the message
     * @param position the error offset, must be non-negative
     */
    public ArgumentBindingException(final String message, final int position) {
        super(message, position);
    }

    /**
     * Construct an instance with specified {@code message} and as caused by a {@link ArgumentParserException}.
     *
     * @param message the message
     * @param cause the cause
     */
    public ArgumentBindingException(final String message, final ArgumentParserException cause) {
        super(message, cause);
    }
}
