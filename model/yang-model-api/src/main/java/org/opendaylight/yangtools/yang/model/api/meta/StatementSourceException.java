/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An exception identifying a problem detected at a particular YANG statement. Exposes {@link #sourceRef()} to that
 * statement.
 */
// FIXME: 15.0.0: retire this exception in favor of UncheckedStatementException
public class StatementSourceException extends RuntimeException implements StatementSourceReferenceAware {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull StatementSourceReference sourceRef;

    public StatementSourceException(final StatementSourceReference sourceRef, final String message) {
        super(StatementException.createMessage(sourceRef, message));
        this.sourceRef = requireNonNull(sourceRef);
    }

    public StatementSourceException(final StatementSourceReference sourceRef, final String message,
            final Throwable cause) {
        super(StatementException.createMessage(sourceRef, message), cause);
        this.sourceRef = requireNonNull(sourceRef);
    }

    public StatementSourceException(final StatementSourceReference sourceRef, final String format,
            final Object... args) {
        this(sourceRef, format.formatted(args));
    }

    public StatementSourceException(final StatementSourceReference sourceRef, final Throwable cause,
            final String format, final Object... args) {
        this(sourceRef, format.formatted(args), cause);
    }

    /**
     * Construct an instance by inheriting message and sourceRef from a {@link StatementException}.
     *
     * @param cause the {@link StatementException}
     * @since 14.0.22
     */
    @NonNullByDefault
    public StatementSourceException(final StatementException cause) {
        super(cause.getMessage(), cause);
        sourceRef = cause.sourceRef();
    }

    /**
     * Construct an instance by inheriting message and sourceRef from a {@link UncheckedStatementException}.
     *
     * @param cause the {@link UncheckedStatementException}
     * @since 14.0.22
     */
    @NonNullByDefault
    public StatementSourceException(final UncheckedStatementException cause) {
        super(cause.getMessage(), cause);
        sourceRef = cause.sourceRef();
    }

    /**
     * {@return the reference to the source which caused this exception}
     */
    @Override
    public final @NonNull StatementSourceReference sourceRef() {
        return sourceRef;
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

    protected final void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }
}
