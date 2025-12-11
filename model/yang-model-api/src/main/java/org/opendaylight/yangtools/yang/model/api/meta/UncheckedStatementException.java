/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static com.google.common.base.Verify.verifyNotNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for uncheck equivalents of {@link StatementException}. This class is not serializable.
 */
public final class UncheckedStatementException extends RuntimeException implements StatementSourceReferenceAware {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor wrapping a {@link StatementException}.
     *
     * @param cause the {@link StatementException}
     */
    @NonNullByDefault
    public UncheckedStatementException(final StatementException cause) {
        super(cause.getMessage(), cause);
    }

    @Override
    public @NonNull String getMessage() {
        return verifyNotNull(super.getMessage());
    }

    @Override
    public @NonNull StatementException getCause() {
        return StatementException.class.cast(super.getCause());
    }

    /**
     * {@return the reference to the source which caused this exception}
     */
    @Override
    public StatementSourceReference sourceRef() {
        return getCause().sourceRef();
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

    protected void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }
}
