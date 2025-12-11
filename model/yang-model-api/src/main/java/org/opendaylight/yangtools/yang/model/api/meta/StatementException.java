/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An abstract base class of checked exceptions which are caused by a specific statement identified by
 * {@link #sourceRef()}. Subclasses of this exception are not serializable.
 *
 * @since 14.0.22
 */
public abstract class StatementException extends Exception implements StatementSourceReferenceAware {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull StatementSourceReference sourceRef;

    @NonNullByDefault
    protected StatementException(final StatementSourceReference sourceRef, final String message) {
        super(createMessage(sourceRef, message));
        this.sourceRef = requireNonNull(sourceRef);
    }

    @NonNullByDefault
    protected StatementException(final StatementSourceReference sourceRef, final String message,
            final Throwable cause) {
        super(createMessage(sourceRef, message), requireNonNull(cause));
        this.sourceRef = requireNonNull(sourceRef);
    }

    @NonNullByDefault
    protected StatementException(final StatementSourceException cause) {
        super(verifyNotNull(cause.getMessage()), cause);
        sourceRef = cause.sourceRef();
    }

    @Override
    public final @NonNull String getMessage() {
        return verifyNotNull(getMessage());
    }

    /**
     * {@return the reference to the source which caused this exception}
     */
    @Override
    public final StatementSourceReference sourceRef() {
        return sourceRef;
    }

    /**
     * {@return {@link UncheckedStatementException} equivalent of this exception}
     */
    public abstract @NonNull UncheckedStatementException toUnchecked();

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

    @NonNullByDefault
    static final String createMessage(final StatementSourceReference sourceRef, final String message) {
        return requireNonNull(message) + " [at " + requireNonNull(sourceRef) + ']';
    }
}
