/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception reported from {@link RegularExpressionParser}.
 */
@NonNullByDefault
public final class RegularExpressionException extends Exception {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final int line;
    private final int charPositionInLine;

    RegularExpressionException(final int line, final int charPositionInLine, final String message,
            final @Nullable Exception cause) {
        super(requireNonNull(message), cause);
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public int line() {
        return line;
    }

    public int charPositionInLine() {
        return charPositionInLine;
    }
}
