/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;

/**
 * A builder for creating RpcResult instances.
 *
 * @author Thomas Pantelis
 *
 * @param <T> the result value type
 */
public final class RpcResultBuilder<T> {

    private static class RpcResultImpl<T> implements RpcResult<T> {

        private final Collection<RpcError> errors;
        private final T result;
        private final boolean successful;

        RpcResultImpl( boolean successful, T result,
                       Collection<RpcError> errors ) {
            this.successful = successful;
            this.result = result;
            this.errors = errors;
        }

        @Override
        public Collection<RpcError> getErrors() {
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
        public String toString(){
            return "RpcResult [successful=" + successful + ", result="
                    + result + ", errors=" + errors + "]";
        }
    }

    private static class RpcErrorImpl implements RpcError {

        private final String applicationTag;
        private final String tag;
        private final String info;
        private final ErrorSeverity severity;
        private final String message;
        private final ErrorType errorType;
        private final Throwable cause;

        RpcErrorImpl( ErrorSeverity severity, ErrorType errorType,
                String tag, String message, String applicationTag, String info,
                Throwable cause ) {
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
        public String getTag() {
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
        public String getMessage(){
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
        public String toString(){
            return "RpcError [message=" + message + ", severity="
                    + severity + ", errorType=" + errorType + ", tag=" + tag
                    + ", applicationTag=" + applicationTag + ", info=" + info
                    + ", cause=" + cause + "]";
        }
    }

    private ImmutableList.Builder<RpcError> errors;
    private T result;
    private final boolean successful;

    private RpcResultBuilder( boolean successful, T result ) {
        this.successful = successful;
        this.result = result;
    }

    /**
     * Returns a builder for a successful result.
     */
    public static <T> RpcResultBuilder<T> success() {
        return new RpcResultBuilder<T>( true, null );
    }

    /**
     * Returns a builder for a successful result.
     *
     * @param result the result value
     */
    public static <T> RpcResultBuilder<T> success( T result ) {
         return new RpcResultBuilder<T>( true, result );
    }

    public static <T> RpcResultBuilder<T> success(Builder<T> result) {
        return success(result.build());
    }

    /**
     * Returns a builder for a failed result.
     */
    public static <T> RpcResultBuilder<T> failed() {
        return new RpcResultBuilder<T>( false, null );
    }

    /**
     * Returns a builder based on the given status.
     *
     * @param success true if successful, false otherwise.
     */
    public static <T> RpcResultBuilder<T> status( boolean success ) {
        return new RpcResultBuilder<T>( success, null );
    }

    /**
     * Returns a builder from another RpcResult.
     *
     * @param other the other RpcResult.
     */
    public static <T> RpcResultBuilder<T> from( RpcResult<T> other ) {
        return new RpcResultBuilder<T>( other.isSuccessful(), other.getResult() )
                                                      .withRpcErrors( other.getErrors() );
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
    public static RpcError newError( ErrorType errorType, String tag, String message ) {
        return new RpcErrorImpl( ErrorSeverity.ERROR, errorType,
                tag != null ? tag : "operation-failed", message, null, null, null );
    }

    /**
     * Creates an RpcError with severity ERROR for reuse.
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param tag a short string that identifies the general type of error condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the error condition.
     * * @param applicationTag a short string that identifies the specific type of error condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the error.
     *
     * @return an RpcError
     */
    public static RpcError newError(  ErrorType errorType, String tag, String message,
            String applicationTag, String info, Throwable cause ) {
        return new RpcErrorImpl( ErrorSeverity.ERROR, errorType,
                tag != null ? tag : "operation-failed", message, applicationTag, info, cause );
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
    public static RpcError newWarning( ErrorType errorType, String tag, String message ) {
        return new RpcErrorImpl( ErrorSeverity.WARNING, errorType, tag, message, null, null, null );
    }

    /**
     * Creates an RpcError with severity WARNING for reuse.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     * * @param applicationTag a short string that identifies the specific type of warning condition.
     * @param info a string containing additional information to provide extended
     *        and/or implementation-specific debugging information.
     * @param cause the exception that triggered the warning.
     *
     * @return an RpcError
     */
    public static RpcError newWarning(  ErrorType errorType, String tag, String message,
            String applicationTag, String info, Throwable cause ) {
        return new RpcErrorImpl( ErrorSeverity.WARNING, errorType, tag, message,
                                 applicationTag, info, cause );
    }

    /**
     * Sets the value of the result.
     *
     * @param result the result value
     */
    public RpcResultBuilder<T> withResult( T result ) {
        this.result = result;
        return this;
    }
    public RpcResultBuilder<T> withResult( Builder<T> result ) {
        return withResult(result.build());
    }

    private void addError( ErrorSeverity severity, ErrorType errorType,
            String tag, String message, String applicationTag, String info,
            Throwable cause ) {

        addError( new RpcErrorImpl( severity, errorType,
                                    tag != null ? tag : "operation-failed", message,
                                    applicationTag, info, cause ) );
    }

    private void addError( RpcError error ) {

        if( errors == null ) {
            errors = new ImmutableList.Builder<RpcError>();
        }

        errors.add( error );
    }

    /**
     * Adds a warning to the result.
     *
     * @param errorType the conceptual layer at which the warning occurred.
     * @param tag a short string that identifies the general type of warning condition. See
     *        {@link RpcError#getTag} for a list of suggested values.
     * @param message a string suitable for human display that describes the warning condition.
     */
    public RpcResultBuilder<T> withWarning( ErrorType errorType, String tag, String message ) {
        addError( ErrorSeverity.WARNING, errorType, tag, message, null, null, null );
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
    public RpcResultBuilder<T> withWarning( ErrorType errorType, String tag, String message,
            String applicationTag, String info, Throwable cause ) {
        addError( ErrorSeverity.WARNING, errorType, tag, message, applicationTag, info, cause );
        return this;
    }

    /**
     * Adds an error to the result. The general error tag defaults to "operation-failed".
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param message a string suitable for human display that describes the error condition.
     */
    public RpcResultBuilder<T> withError( ErrorType errorType, String message ) {
        addError( ErrorSeverity.ERROR, errorType, null, message, null, null, null );
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
    public RpcResultBuilder<T> withError( ErrorType errorType, String tag, String message ) {
        addError( ErrorSeverity.ERROR, errorType, tag, message, null, null, null );
        return this;
    }

    /**
     * Adds an error to the result. The general error tag defaults to "operation-failed".
     *
     * @param errorType the conceptual layer at which the error occurred.
     * @param message a string suitable for human display that describes the error condition.
     * @param cause the exception that triggered the error.
     */
    public RpcResultBuilder<T> withError( ErrorType errorType, String message,
                                          Throwable cause ) {
        addError( ErrorSeverity.ERROR, errorType, null, message, null, null, cause );
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
    public RpcResultBuilder<T> withError( ErrorType errorType, String tag, String message,
            String applicationTag, String info, Throwable cause ) {
        addError( ErrorSeverity.ERROR, errorType, tag, message, applicationTag, info, cause );
        return this;
    }

    /**
     * Adds an RpcError.
     *
     * @param error the RpcError
     */
    public RpcResultBuilder<T> withRpcError( RpcError error ) {
        addError( error );
        return this;
    }

    /**
     * Adds RpcErrors.
     *
     * @param errors the list of RpcErrors
     */
    public RpcResultBuilder<T> withRpcErrors( Collection<RpcError> errors ) {
        if( errors != null ) {
            for( RpcError error: errors ) {
                addError( error );
            }
        }
        return this;
    }

    public RpcResult<T> build() {

        return new RpcResultImpl<T>( successful, result,
                errors != null ? errors.build() : Collections.<RpcError>emptyList() );
    }

    public Future<RpcResult<T>> buildFuture() {
        return Futures.immediateFuture(build());
    }
}
