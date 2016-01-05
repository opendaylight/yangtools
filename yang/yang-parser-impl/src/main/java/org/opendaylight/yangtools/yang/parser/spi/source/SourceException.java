/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

/**
 * Thrown to indicate error in YANG model source.
 */
public class SourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final StatementSourceReference sourceRef;

    public SourceException(@Nonnull final String message, @Nonnull final StatementSourceReference source) {
        super(Preconditions.checkNotNull(message));
        sourceRef = Preconditions.checkNotNull(source);
    }

    public SourceException(@Nonnull final String message, @Nonnull final StatementSourceReference source,
            final Throwable cause) {
        super(Preconditions.checkNotNull(message),cause);
        sourceRef = Preconditions.checkNotNull(source);
    }

    public SourceException(@Nonnull final StatementSourceReference source, @Nonnull final String format, final Object... args) {
        this(String.format(format, args), source);
    }

    public SourceException(@Nonnull final StatementSourceReference source, final Throwable cause,
            @Nonnull final String format, final Object... args) {
        this(String.format(format, args), source, cause);
    }

    public @Nonnull StatementSourceReference getSourceReference() {
        return sourceRef;
    }

    public static void check(final boolean expression, @Nonnull final StatementSourceReference source,
            @Nonnull final String format, final Object... args) {
        if (!expression) {
            throw new SourceException(source, format, args);
        }
    }

    public static <T> T checkNotNull(final T obj, @Nonnull final StatementSourceReference source,
            @Nonnull final String format, final Object... args) {
        if (obj == null) {
            throw new SourceException(source, format, args);
        }
        return obj;
    }
}
