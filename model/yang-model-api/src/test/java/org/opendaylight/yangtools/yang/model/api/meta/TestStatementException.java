/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class TestStatementException extends StatementException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    TestStatementException(final StatementSourceReference sourceRef, final String message) {
        super(sourceRef, message);
    }

    TestStatementException(final StatementSourceReference sourceRef, final String message, final Throwable cause) {
        super(sourceRef, message, cause);
    }

    TestStatementException(final StatementSourceException cause) {
        super(cause);
    }
}
