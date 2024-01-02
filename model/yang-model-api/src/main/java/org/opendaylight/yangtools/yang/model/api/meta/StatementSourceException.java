/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

/**
 *
 */
public class StatementSourceException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull StatementSourceReference sourceRef;

    public StatementSourceException(final @NonNull StatementSourceReference sourceRef, final String message) {
        super(message);
        this.sourceRef = requireNonNull(sourceRef);
    }

    public StatementSourceException(final @NonNull StatementSourceReference sourceRef, final String message,
            final Throwable cause) {
        super(message, cause);
        this.sourceRef = requireNonNull(sourceRef);
    }

    /**
     * Return the reference to the source which caused this exception.
     *
     * @return the reference to the source which caused this exception
     */
    public final @NonNull StatementSourceReference sourceRef() {
        return sourceRef;
    }
}
