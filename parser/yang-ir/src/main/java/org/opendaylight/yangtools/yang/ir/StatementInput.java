/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import static java.util.Objects.requireNonNull;

import java.io.DataInput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
abstract sealed class StatementInput permits StatementInputV1 {
    final DataInput in;

    StatementInput(final DataInput in) {
        this.in = requireNonNull(in);
    }

    abstract IRStatement readStatement() throws IOException;
}
