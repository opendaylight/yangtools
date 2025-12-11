/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class TestUncheckedStatementException extends UncheckedStatementException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    TestUncheckedStatementException(final TestStatementException cause) {
        super(cause);
    }

    @Override
    public @NonNull TestStatementException getCause() {
        return TestStatementException.class.cast(super.getCause());
    }

}
