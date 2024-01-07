/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Thrown when there was invalid element in YANG file.
 */
@NonNullByDefault
public class InvalidSubstatementException extends SourceException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public InvalidSubstatementException(final String message, final StatementSourceReference source) {
        super(message, source);
    }

    public InvalidSubstatementException(final String message, final StatementSourceReference source,
            final Throwable cause) {
        super(message, source, cause);
    }

    public InvalidSubstatementException(final StatementSourceReference source, final String format,
            final Object... args) {
        super(source, format, args);
    }
}
