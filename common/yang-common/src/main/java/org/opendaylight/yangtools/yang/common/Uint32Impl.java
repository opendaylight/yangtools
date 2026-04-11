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
 * Default implementation of {@link Uint32}.
 */
// TODO: value class when we have JEP-401 available
@NonNullByDefault
final class Uint32Impl extends Uint32 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    Uint32Impl(final int value) {
        super(value);
    }
}
