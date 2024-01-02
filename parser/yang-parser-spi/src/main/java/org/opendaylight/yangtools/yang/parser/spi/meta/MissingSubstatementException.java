/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Thrown when there was Missing element in yang file.
 */
public class MissingSubstatementException extends SourceException {
    private static final long serialVersionUID = 1L;

    public MissingSubstatementException(final @NonNull String message, final @NonNull StatementSourceReference source) {
        super(message, source);
    }

    public MissingSubstatementException(final @NonNull String message, final @NonNull StatementSourceReference source,
            final Throwable cause) {
        super(message, source, cause);
    }

    public MissingSubstatementException(final @NonNull CommonStmtCtx stmt, final @NonNull String format,
            final Object... args) {
        this(stmt.sourceReference(), format, args);
    }

    public MissingSubstatementException(final @NonNull StatementSourceReference source, final @NonNull String format,
            final Object... args) {
        this(String.format(format, args), source);
    }
}
