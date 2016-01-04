/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * Thrown when there was Invalid element in yang file
 */
public class InvalidSubstatementException extends SourceException {
    private static final long serialVersionUID = 1L;

    public InvalidSubstatementException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final Throwable cause) {
        super(message, source, cause);
    }

    public InvalidSubstatementException(@Nonnull final String message, @Nonnull final StatementSourceReference source) {
        super(message, source);
    }

    public InvalidSubstatementException(@Nonnull final StatementSourceReference source, @Nonnull final String format,
            final Object... args) {
        this(String.format(format, args), source);
    }
}
