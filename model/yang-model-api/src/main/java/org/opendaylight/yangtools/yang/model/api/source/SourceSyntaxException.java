/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * An exception thrown when a {@link SourceRepresentation} is found to contain syntax errors.
 */
public final class SourceSyntaxException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @Nullable StatementSourceReference sourceRef;

    public SourceSyntaxException(final String message) {
        this(message, null, null);
    }

    public SourceSyntaxException(final String message, final @Nullable Throwable cause) {
        this(message, cause, null);
    }

    public SourceSyntaxException(final String message, final @Nullable StatementSourceReference sourceRef) {
        this(message, null, sourceRef);
    }

    public SourceSyntaxException(final String message, final @Nullable Throwable cause,
            final @Nullable StatementSourceReference sourceRef) {
        super(createMessage(requireNonNull(message), sourceRef), cause);
        this.sourceRef = sourceRef;
    }

    private static String createMessage(final String message, final @Nullable StatementSourceReference sourceRef) {
        return sourceRef == null ? message : message + " [at " + sourceRef + ']';
    }

    /**
     * {@return the {@link StatementSourceReference} to the statement causing this exception, or {@code null} when
     * not available}
     */
    public @Nullable StatementSourceReference sourceRef() {
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

    private static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(SourceSyntaxException.class.getName());
    }
}
