/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An exception reported when the {@code prefix} part of a {@code node-identifier} cannot be resolved to a module.
 */
@NonNullByDefault
public final class UnknownPrefixException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final int errorOffset;

    /**
     * Default constructor.
     *
     * @param message the message
     * @param errorOffset the errorOffset
     */
    public UnknownPrefixException(final String message, final int errorOffset) {
        super(requireNonNull(message));
        this.errorOffset = errorOffset;
    }

    /**
     * {@return the position where the error was found}
     */
    // Note: same name as used in ParseException
    public int getErrorOffset() {
        return errorOffset;
    }
}