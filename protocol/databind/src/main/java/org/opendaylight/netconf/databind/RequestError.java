/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;

/**
 * A basic request error, as defined in both
 * <a href="https://www.rfc-editor.org/rfc/rfc6241#section-4.3">RFC6241 rpc-error element</a> and a single {@code error}
 * within the <a href="https://www.rfc-editor.org/rfc/rfc8040#section-3.9">RFC8040 "errors" YANG Data Template</a>.
 *
 * @param type value of {@code error-type} leaf
 * @param tag value of {@code error-tag} leaf
 * @param message value of {@code error-message} leaf, potentially with metadata
 * @param appTag value of {@code error-api-tag} leaf
 * @param path optional {@code error-path} leaf
 * @param info optional content of {@code error-info} anydata
 */
@NonNullByDefault
public record RequestError(
        ErrorType type,
        ErrorTag tag,
        @Nullable ErrorMessage message,
        @Nullable String appTag,
        @Nullable ErrorPath path,
        @Nullable ErrorInfo info) {
    public RequestError {
        requireNonNull(type);
        requireNonNull(tag);
    }

    public RequestError(final ErrorType type, final ErrorTag tag, final String message) {
        this(type, tag, new ErrorMessage(message), null, null, null);
    }

    /**
     * {@return a RequestError equivalent of specified {@link RpcError}}
     * @param rpcError the {@link RpcError}
     */
    public static RequestError ofRpcError(final RpcError rpcError) {
        return ofRpcError(rpcError, null);
    }

    /**
     * {@return a RequestError equivalent of specified {@link RpcError} with an optional {@link ErrorPath}}
     * @param rpcError the {@link RpcError}
     * @param path the {@link ErrorPath}
     */
    public static RequestError ofRpcError(final RpcError rpcError, final @Nullable ErrorPath path) {
        final var tag = rpcError.getTag();
        final var errorTag = tag != null ? tag : ErrorTag.OPERATION_FAILED;
        final var errorMessage = rpcError.getMessage();
        return new RequestError(rpcError.getErrorType(), errorTag,
            errorMessage != null ? new ErrorMessage(errorMessage) : null, rpcError.getApplicationTag(), path,
            extractErrorInfo(rpcError));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("type", type)
            .add("tag", tag)
            .add("appTag", appTag)
            .add("message", message)
            .add("path", path)
            .add("info", info)
            .toString();
    }

    private static @Nullable ErrorInfo extractErrorInfo(final RpcError rpcError) {
        final var info = rpcError.getInfo();
        if (info != null) {
            return new ErrorInfo(info);
        }
        final var cause = rpcError.getCause();
        return cause != null ? new ErrorInfo(cause.getMessage()) : null;
    }
}
