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

/**
 * An exception identifying a problem detected at a particular YANG statement. Exposes {@link #sourceRef()} to that
 * statement.
 */
public class StatementSourceException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull StatementSourceReference sourceRef;

    public StatementSourceException(final StatementSourceReference sourceRef, final String message) {
        super(createMessage(sourceRef, message));
        this.sourceRef = requireNonNull(sourceRef);
    }

    public StatementSourceException(final StatementSourceReference sourceRef, final String message,
            final Throwable cause) {
        super(createMessage(sourceRef, message), cause);
        this.sourceRef = requireNonNull(sourceRef);
    }

    public StatementSourceException(final StatementSourceReference sourceRef, final String format,
            final Object... args) {
        this(sourceRef, format.formatted(args));
    }

    private static String createMessage(final StatementSourceReference sourceRef, final String message) {
        return requireNonNull(message) + " [at " + requireNonNull(sourceRef) + ']';
    }

    /**
     * Return the reference to the source which caused this exception.
     *
     * @return the reference to the source which caused this exception
     */
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
