/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

/**
 * Exception thrown from NormalizedNodeInputStreamReader when the input stream does not contain
 * valid serialized data.
 *
 * @author Thomas Pantelis
 */
public class InvalidNormalizedNodeStreamException extends IOException {
    private static final long serialVersionUID = 1L;

    /**
     * Construct an instance with a detail message.
     *
     * @param message the detail message
     */
    public InvalidNormalizedNodeStreamException(final String message) {
        super(requireNonNull(message));
    }

    /**
     * Construct an instance with a detail message and an optional cause.
     *
     * @param message the detail message
     * @param cause the cause, {@code null} if not available
     */
    public InvalidNormalizedNodeStreamException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
    }
}
