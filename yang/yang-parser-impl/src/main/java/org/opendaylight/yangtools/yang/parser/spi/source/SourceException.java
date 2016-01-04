/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import javax.annotation.Nonnull;

/**
 *
 * Thrown to indicate error in YANG model source.
 *
 */
public class SourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final StatementSourceReference sourceRef;

    public SourceException(@Nonnull String message,@Nonnull StatementSourceReference source) {
        super(Preconditions.checkNotNull(message));
        sourceRef = Preconditions.checkNotNull(source);
    }

    public SourceException(@Nonnull String message,@Nonnull StatementSourceReference source, Throwable cause) {
        super(Preconditions.checkNotNull(message),cause);
        sourceRef = Preconditions.checkNotNull(source);
    }
    
    public SourceException(@Nonnull StatementSourceReference source, String format, Object... args) {
        this(String.format(format, args), source);
    }

    public SourceException(@Nonnull StatementSourceReference source, Throwable cause, String format, Object... args) {
        this(String.format(format, args), source, cause);
    }
    
    public @Nonnull StatementSourceReference getSourceReference() {
        return sourceRef;
    }

    public static void check(boolean expression, StatementSourceReference source, String format, Object... args) {
        if (!expression) {
            throw new SourceException(source, format, args);
        }
    }
    
    public static void checkNotNull(final Object obj, StatementSourceReference source, String format, Object... args) {
        if (obj == null) {
            throw new SourceException(source, format, args);
        }
    }
}
