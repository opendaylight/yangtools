/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception reported by implementations of this API.
 *
 * @since 16.2.0
 */
@NonNullByDefault
public final class CodecException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    CodecException(final String message) {
        super(requireNonNull(message));
    }

    CodecException(final String message, final Exception cause) {
        super(requireNonNull(message), requireNonNull(cause));
    }

    CodecException(final @Nullable Exception cause, final String fmt, final Object... args) {
        super(fmt.formatted(args), cause);
    }

    // FIXME: not serializable
}
