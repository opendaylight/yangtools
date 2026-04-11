/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default implementation of {@link Uint8}.
 */
@NonNullByDefault
value class Uint8Impl extends Uint8 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    Uint8Impl(final byte value) {
        super(value);
    }
}
