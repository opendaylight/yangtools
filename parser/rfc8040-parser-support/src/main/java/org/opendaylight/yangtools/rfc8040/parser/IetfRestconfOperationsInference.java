/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

// FIXME: responsible for creating leaf statements
final class IetfRestconfOperationsInference {
    private IetfRestconfOperationsInference() {
        // Hidden on purpose
    }

    static void applyTo(final Mutable<?, ?, ?> operations) {
        System.out.println("");
    }
}
