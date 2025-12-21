/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for exceptions reported by {@link ArgumentParser} implementations.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public abstract sealed class ArgumentParserException extends Exception
        permits ArgumentBindingException, ArgumentSyntaxException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final int position;

    /**
     * Construct an instance with specified {@code message} and {@code position} and an optional cause.
     *
     * @param message the message
     * @param position the error offset, must be non-negative
     * @param cause the cause, {@code null} if not present
     */
    ArgumentParserException(final String message, final int position, final @Nullable Exception cause) {
        super(requireNonNull(message), cause);
        this.position = position;
        validate();
    }

    /**
     * Construct an instance with specified {@code message} and {@code position}.
     *
     * @param message the message
     * @param position the error offset, must be non-negative
     */
    ArgumentParserException(final String message, final int position) {
        this(message, position, null);
    }

    /**
     * Construct an instance with specified {@code message} and as caused by a {@link ArgumentParserException}.
     *
     * @param message the message
     * @param position the error offset, must be non-negative
     */
    ArgumentParserException(final String message, final ArgumentParserException cause) {
        this(message, cause.getPosition(), cause);
    }

    @Override
    public final String getMessage() {
        return verifyNotNull(super.getMessage());
    }

    /**
     * {@return the character position where the error was found, {code 0} means unknown}
     */
    public final int getPosition() {
        return position;
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        validate();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Stream data required");
    }

    private void validate() {
        if (position < 0) {
            throw new IllegalArgumentException("negative position=" + position);
        }
    }
}
