/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;

/**
 * This exception is typically reported by methods which normalize some external format into a
 * {@link NormalizationResult}. It can be mapped to one or more {@link YangNetconfError}s.
 */
public final class NormalizationException extends Exception implements YangNetconfErrorAware {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull List<@NonNull YangNetconfError> netconfErrors;

    private NormalizationException(final Throwable cause) {
        super(cause);
        netconfErrors = constructErrors(getMessage());
    }

    private NormalizationException(final String message, final Throwable cause,
            final List<@NonNull YangNetconfError> netconfErrors) {
        super(message, cause);
        this.netconfErrors = requireNonNull(netconfErrors);
    }

    public static @NonNull NormalizationException ofMessage(final String message) {
        return new NormalizationException(requireNonNull(message), null, constructErrors(message));
    }

    public static @NonNull NormalizationException ofCause(final Throwable cause) {
        if (cause instanceof YangNetconfErrorAware aware) {
            final var errors = aware.getNetconfErrors();
            if (!errors.isEmpty()) {
                return new NormalizationException(cause.getMessage(), cause, List.copyOf(errors));
            }
        }
        return new NormalizationException(cause);
    }

    @Override
    public List<@NonNull YangNetconfError> getNetconfErrors() {
        return netconfErrors;
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }

    private static @NonNull List<@NonNull YangNetconfError> constructErrors(final @NonNull String message) {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.PROTOCOL)
            .tag(ErrorTag.MALFORMED_MESSAGE)
            .message(message)
            .build());
    }

    private static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(NormalizationException.class.getName());
    }
}
