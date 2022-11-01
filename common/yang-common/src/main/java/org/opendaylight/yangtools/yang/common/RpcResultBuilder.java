/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A builder for creating RpcResult instances.
 *
 * @author Thomas Pantelis
 *
 * @param <T> the result value type
 */
public final class RpcResultBuilder<T> implements Mutable {

    private static class RpcResultImpl<T> implements RpcResult<T>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final ImmutableList<RpcError> errors;
        private final T result;
        private final boolean successful;

        RpcResultImpl(final boolean successful, final T result, final ImmutableList<RpcError> errors) {
            this.successful = successful;
            this.result = result;
            this.errors = requireNonNull(errors);
        }

        @Override
        public ImmutableList<RpcError> getErrors() {
            return errors;
        }

        @Override
        public T getResult() {
            return result;
        }

        @Override
        public boolean isSuccessful() {
            return successful;
        }

        @Override
        public String toString() {
            return "RpcResult [successful=" + successful + ", result="
                    + result + ", errors=" + errors + "]";
        }
    }

    private static class RpcErrorImpl implements RpcError, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String applicationTag;
        private final ErrorTag tag;
        private final String info;
        private final ErrorSeverity severity;
        private final String message;
        private final ErrorType errorType;
        private final Throwable cause;

        RpcErrorImpl(final ErrorSeverity severity, final ErrorType errorType, final ErrorTag tag, final String message,
                final String applicationTag, final String info, final Throwable cause) {
            this.severity = severity;
            this.errorType = errorType;
            this.tag = tag;
            this.message = message;
            this.applicationTag = applicationTag;
            this.info = info;
            this.cause = cause;
        }

        @Override
        public String getApplicationTag() {
            return applicationTag;
        }

        @Override
        public ErrorTag getTag() {
            return tag;
        }

        @Override
        public String getInfo() {
            return info;
        }

        @Override
        public ErrorSeverity getSeverity() {
            return severity;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public ErrorType getErrorType() {
            return errorType;
        }

        @Override
        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return "RpcError [message=" + message + ", severity="
                    + severity + ", errorType=" + errorType + ", tag=" + tag
                    + ", applicationTag=" + applicationTag + ", info=" + info
                    + ", cause=" + cause + "]";
        }
    }

    private ImmutableList.Builder<RpcError> errors;
    private T result;
    private final boolean successful;

    private RpcResultBuilder(final boolean successful, final T result) {
        this.successful = successful;
        this.result = result;
    }

    /**
     * Returns a builder for a successful result.
     */
    public static <T> @NonNull RpcResultBuilder<T> success() {
        return new RpcResultBuilder<>(true, null);
    }

    /**
     * Returns a builder for a successful result.
     *
     * @param result the result value
     */
    public static <T> @NonNull RpcResultBuilder<T> success(final T result) {
        return new RpcResultBuilder<>(true, result);
    }

    /**
     * Returns a builder for a failed result.
     */
    public static <T> @NonNull RpcResultBuilder<T> failed() {
        return new RpcResultBuilder<>(false, null);
    }

    /**
     * Returns a builder based on the given status.
     *
     * @param success true if successful, false otherwise.
     */
    public static <T> @NonNull RpcResultBuilder<T> status(final boolean success) {
        return new RpcResultBuilder<>(success, null);
    }

    /**
     * Returns a builder from another RpcResult.
     *
     * @param other the other RpcResult.
     */
    public static <T> @NonNull RpcResultBuilder<T> from(final RpcResult<T> other) {
        return new RpcResultBuilder<>(other.isSuccessful(), other.getResult())
                                                      .withRpcErrors(other.getErrors());
    }

    /**
     * Creates an RpcError with severity ERROR for reuse.
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param tag a short string that identifies the general type of error condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the error condition.
     *
     * @return an RpcError
     */
    public static @NonNull RpcError newError(final ErrorType errorType, final ErrorTag tag, final String message) {
        return new RpcErrorImpl(ErrorSeverity.ERROR, errorType, tag != null ? tag : ErrorTag.OPERATION_FAILED, message,
            null, null, null);
    }

    /**
     * Creates an RpcError with severity ERROR for reuse.
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param tag a short string that identifies the general type of error condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the error condition.
     * @param applicationTag a short string that identifies the specific type of error condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the error.
     *
     * @return an RpcError
     */
    public static @NonNull RpcError newError(final ErrorType errorType, final ErrorTag tag, final String message,
            final String applicationTag, final String info, final Throwable cause) {
        return new RpcErrorImpl(ErrorSeverity.ERROR, errorType, tag != null ? tag : ErrorTag.OPERATION_FAILED, message,
            applicationTag, info, cause);
    }

    /**
     * Creates an RpcError with severity WARNING for reuse.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     *
     * @return an RpcError
     */
    public static @NonNull RpcError newWarning(final ErrorType errorType, final ErrorTag tag, final String message) {
        return new RpcErrorImpl(ErrorSeverity.WARNING, errorType, tag, message, null, null, null);
    }

    /**
     * Creates an RpcError with severity WARNING for reuse.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     * @param applicationTag a short string that identifies the specific type of warning condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the warning.
     *
     * @return an RpcError
     */
    public static @NonNull RpcError newWarning(final ErrorType errorType, final ErrorTag tag, final String message,
            final String applicationTag, final String info, final Throwable cause) {
        return new RpcErrorImpl(ErrorSeverity.WARNING, errorType, tag, message, applicationTag, info, cause);
    }

    /**
     * Sets the value of the result.
     *
     * @param result the result value
     */
    @SuppressWarnings("checkstyle:hiddenField")
    public @NonNull RpcResultBuilder<T> withResult(final T result) {
        this.result = result;
        return this;
    }

    private void addError(final ErrorSeverity severity, final ErrorType errorType, final ErrorTag tag,
            final String message, final String applicationTag, final String info, final Throwable cause) {
        addError(new RpcErrorImpl(severity, errorType, tag != null ? tag : ErrorTag.OPERATION_FAILED, message,
            applicationTag, info, cause));
    }

    private void addError(final RpcError error) {

        if (errors == null) {
            errors = new ImmutableList.Builder<>();
        }

        errors.add(error);
    }

    /**
     * Adds a warning to the result.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     */
    public @NonNull RpcResultBuilder<T> withWarning(final ErrorType errorType, final ErrorTag tag,
            final String message) {
        addError(ErrorSeverity.WARNING, errorType, tag, message, null, null, null);
        return this;
    }

    /**
     * Adds a warning to the result.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     * @param applicationTag a short string that identifies the specific type of warning condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the warning.
     */
    public @NonNull RpcResultBuilder<T> withWarning(final ErrorType errorType, final ErrorTag tag, final String message,
            final String applicationTag, final String info, final Throwable cause) {
        addError(ErrorSeverity.WARNING, errorType, tag, message, applicationTag, info, cause);
        return this;
    }

    /**
     * Adds an error to the result. The general error tag defaults to "operation-failed".
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param message a string suitable for human display that describes the error condition.
     */
    public @NonNull RpcResultBuilder<T> withError(final ErrorType errorType, final String message) {
        addError(ErrorSeverity.ERROR, errorType, null, message, null, null, null);
        return this;
    }

    /**
     * Adds an error to the result.
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param tag a short string that identifies the general type of error condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the error condition.
     */
    public @NonNull RpcResultBuilder<T> withError(final ErrorType errorType, final ErrorTag tag, final String message) {
        addError(ErrorSeverity.ERROR, errorType, tag, message, null, null, null);
        return this;
    }

    /**
     * Adds an error to the result. The general error tag defaults to "operation-failed".
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param message a string suitable for human display that describes the error condition.
     * @param cause the exception that triggered the error.
     */
    public @NonNull RpcResultBuilder<T> withError(final ErrorType errorType, final String message,
                                          final Throwable cause) {
        addError(ErrorSeverity.ERROR, errorType, null, message, null, null, cause);
        return this;
    }

    /**
     * Adds an error to the result.
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param tag a short string that identifies the general type of error condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the error condition.
     * @param applicationTag a short string that identifies the specific type of error condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the error.
     */
    public @NonNull RpcResultBuilder<T> withError(final ErrorType errorType, final ErrorTag tag, final String message,
            final String applicationTag, final String info, final Throwable cause) {
        addError(ErrorSeverity.ERROR, errorType, tag, message, applicationTag, info, cause);
        return this;
    }

    /**
     * Adds an RpcError.
     *
     * @param error the RpcError
     */
    public @NonNull RpcResultBuilder<T> withRpcError(final RpcError error) {
        addError(error);
        return this;
    }

    /**
     * Adds RpcErrors.
     *
     * @param rpcErrors the list of RpcErrors
     */
    public RpcResultBuilder<T> withRpcErrors(final Collection<? extends RpcError> rpcErrors) {
        if (rpcErrors != null) {
            for (RpcError error : rpcErrors) {
                addError(error);
            }
        }
        return this;
    }

    /**
     * Build the resulting {@link RpcResult}.
     *
     * @return An RpcResult instance
     */
    public @NonNull RpcResult<T> build() {
        return new RpcResultImpl<>(successful, result, errors != null ? errors.build() : ImmutableList.of());
    }

    /**
     * Builds RpcResult and wraps it in a Future.
     *
     * <p>
     * This is a convenience method to assist those writing RPCs that produce immediate results. It allows you to
     * replace {@code FluentFuture.from(Futures.immediateFuture(rpcResult.build()))} with
     * {@code rpcResult.buildFuture()}
     *
     * @return Future for RpcResult built by RpcResultBuilder
     */
    public @NonNull FluentFuture<RpcResult<T>> buildFuture() {
        return FluentFuture.from(Futures.immediateFuture(build()));
    }
}
