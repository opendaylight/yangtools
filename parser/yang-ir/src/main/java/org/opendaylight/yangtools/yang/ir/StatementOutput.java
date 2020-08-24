/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import static java.util.Objects.requireNonNull;

import java.io.DataOutput;
import java.io.IOException;

abstract class StatementOutput {
    final DataOutput out;

    StatementOutput(final DataOutput out) {
        this.out = requireNonNull(out);
    }

    abstract void writeStatement(IRStatement stmt) throws IOException;
}
