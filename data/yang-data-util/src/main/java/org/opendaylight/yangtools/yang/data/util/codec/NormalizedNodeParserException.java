/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;

/**
 * Exception thrown from {@link NormalizedNodeParser} methods.
 */
public final class NormalizedNodeParserException extends Exception implements YangNetconfErrorAware {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull List<@NonNull YangNetconfError> netconfErrors;

    private NormalizedNodeParserException(final Throwable cause) {
        super(cause);
        netconfErrors = constructErrors(getMessage());
    }

    private NormalizedNodeParserException(final String message, final Throwable cause,
            final List<@NonNull YangNetconfError> netconfErrors) {
        super(message, cause);
        this.netconfErrors = requireNonNull(netconfErrors);
    }

    public static @NonNull NormalizedNodeParserException ofMessage(final String message) {
        return new NormalizedNodeParserException(requireNonNull(message), null, constructErrors(message));
    }

    public static @NonNull NormalizedNodeParserException ofCause(final Throwable cause) {
        if (cause instanceof YangNetconfErrorAware aware) {
            final var errors = aware.getNetconfErrors();
            if (!errors.isEmpty()) {
                return new NormalizedNodeParserException(cause.getMessage(), cause, List.copyOf(errors));
            }
        }
        return new NormalizedNodeParserException(cause);
    }

    @Override
    public List<@NonNull YangNetconfError> getNetconfErrors() {
        return netconfErrors;
    }

    private static @NonNull List<@NonNull YangNetconfError> constructErrors(final @NonNull String message) {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.PROTOCOL)
            .tag(ErrorTag.MALFORMED_MESSAGE)
            .message(message)
            .build());
    }
}
