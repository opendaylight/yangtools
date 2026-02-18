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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReferenceAware;

/**
 * An exception thrown when a {@link SourceRepresentation} is found to contain syntax errors.
 * @since 15.0.0
 */
public final class SourceSyntaxException extends Exception implements StatementSourceReferenceAware {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull StatementSourceReference sourceRef;

    public SourceSyntaxException(final String message, final SourceIdentifier sourceId) {
        this(message, sourceId == null ? null : sourceId.toReference(), null);
    }

    public SourceSyntaxException(final String message, final StatementSourceReference sourceRef) {
        this(message, sourceRef, null);
    }

    public SourceSyntaxException(final String message,  final SourceIdentifier sourceId,
            final @Nullable Throwable cause) {
        this(message, sourceId == null ? null : sourceId.toReference(), cause);
    }

    public SourceSyntaxException(final String message, final StatementSourceReference sourceRef,
            final @Nullable Throwable cause) {
        super(createMessage(requireNonNull(message), sourceRef), cause);
        this.sourceRef = requireNonNull(sourceRef);
    }

    private static String createMessage(final String message, final StatementSourceReference sourceRef) {
        return message + " [at " + sourceRef + ']';
    }

    @Override
    public StatementSourceReference sourceRef() {
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
