/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.YangError;

@Beta
public class IllegalYangValueException extends IllegalAccessException implements YangError {
    private static final long serialVersionUID = 1L;

    private final @NonNull ErrorSeverity severity;
    private final @NonNull ErrorType errorType;
    private final @Nullable String errorAppTag;
    private final @Nullable String errorMessage;

    public IllegalYangValueException(final ErrorSeverity severity, final ErrorType errorType, final String message) {
        this(severity, errorType, message, null);
    }

    public IllegalYangValueException(final ErrorSeverity severity, final ErrorType errorType, final String message,
            final @Nullable String errorAppTag) {
        super(message);
        this.severity = requireNonNull(severity);
        this.errorType = requireNonNull(errorType);
        this.errorAppTag = errorAppTag;
        this.errorMessage = null;
    }

    @Override
    public final ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public final ErrorSeverity getSeverity() {
        return severity;
    }

    @Override
    public final Optional<String> getErrorAppTag() {
        return Optional.ofNullable(errorAppTag);
    }

    @Override
    public final Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

}
