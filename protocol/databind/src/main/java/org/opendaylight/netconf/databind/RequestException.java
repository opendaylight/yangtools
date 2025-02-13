/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;

/**
 * A request processing exception, reporting one or more {@link RequestError}s. This exception is not serializable on
 * purpose.
 */
@NonNullByDefault
public final class RequestException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 0L;

    @SuppressWarnings("serial")
    private final List<RequestError> errors;

    private RequestException(final String message, final RequestError error, final @Nullable Throwable cause) {
        super(message, cause);
        errors = List.of(error);
    }

    public RequestException(final String message) {
        this(ErrorType.APPLICATION, ErrorTag.OPERATION_FAILED, requireNonNull(message));
    }

    public RequestException(final String format, final Object @Nullable ... args) {
        this(ErrorType.APPLICATION, ErrorTag.OPERATION_FAILED, format, args);
    }

    public RequestException(final Throwable cause) {
        this(ErrorType.APPLICATION, errorTagOf(cause), cause);
    }

    public RequestException(final String message, final @Nullable Throwable cause) {
        this(ErrorType.APPLICATION, errorTagOf(cause), requireNonNull(message), cause);
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final String message) {
        this(type, tag, message, (Throwable) null);
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final Throwable cause) {
        this(cause.toString(), new RequestError(type, tag, new ErrorMessage(cause.getMessage()), null, null, null),
            cause);
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final String message,
            final @Nullable Throwable cause) {
        this(requireNonNull(message),
            new RequestError(type, tag, new ErrorMessage(message), null, null, errorInfoOf(cause)), cause);
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final String format,
            final Object @Nullable ... args) {
        this(type, tag, format.formatted(args));
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final String message,
            final @Nullable ErrorPath path) {
        this(type, tag, message, path, null);
    }

    public RequestException(final ErrorType type, final ErrorTag tag, final String message,
            final @Nullable ErrorPath path, final @Nullable Throwable cause) {
        this(message, new RequestError(type, tag, new ErrorMessage(message), null, path, errorInfoOf(cause)), cause);
    }

    public RequestException(final List<RequestError> errors, final @Nullable Throwable cause, final String message) {
        super(message, cause);
        this.errors = requireNonNull(errors);
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("empty errors");
        }
    }

    public RequestException(final List<RequestError> errors, final @Nullable Throwable cause, final String format,
            final Object... args) {
        this(errors, cause, format.formatted(args));
    }

    /**
     * Return the reported {@link RequestError}.
     *
     * @return the reported {@link RequestError}
     */
    public List<RequestError> errors() {
        return errors;
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throw new NotSerializableException();
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private static @Nullable ErrorInfo errorInfoOf(final @Nullable Throwable cause) {
        if (cause != null) {
            final var message = cause.getMessage();
            if (message != null) {
                return new ErrorInfo(message);
            }
        }
        return null;
    }

    private static ErrorTag errorTagOf(final @Nullable Throwable cause) {
        if (cause instanceof UnsupportedOperationException) {
            return ErrorTag.OPERATION_NOT_SUPPORTED;
        } else if (cause instanceof IllegalArgumentException) {
            return ErrorTag.INVALID_VALUE;
        } else {
            return ErrorTag.OPERATION_FAILED;
        }
    }
}
