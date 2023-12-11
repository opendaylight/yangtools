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

    private final @NonNull List<YangNetconfError> netconfErrors;

    public NormalizedNodeParserException(final List<YangNetconfError> netconfErrors) {
        this(netconfErrors, null);
    }

    public NormalizedNodeParserException(final List<YangNetconfError> netconfErrors, final Throwable cause) {
        super(extractMessage(netconfErrors), cause);
        this.netconfErrors = List.copyOf(netconfErrors);
    }

    public NormalizedNodeParserException(final ErrorType type, final ErrorTag tag, final String message) {
        this(type, tag, message, null);
    }

    public NormalizedNodeParserException(final ErrorType type, final ErrorTag tag, final String message,
            final Throwable cause) {
        super(requireNonNull(message), cause);
        netconfErrors = List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(type)
            .tag(tag)
            .message(message)
            .build());
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return netconfErrors;
    }

    private static String extractMessage(final List<YangNetconfError> netconfErrors) {
        if (netconfErrors.isEmpty()) {
            throw new IllegalArgumentException("Error list must not be empty");
        }
        return netconfErrors.get(0).message();
    }
}
