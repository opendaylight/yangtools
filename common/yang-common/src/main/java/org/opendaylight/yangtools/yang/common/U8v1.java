/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Serializable;

/**
 * Serialization proxy for {@link Uint64}.
 */
record U8v1(long bits) implements Serializable {
    @java.io.Serial
    private Object readResolve() {
        return Uint64.fromLongBits(bits);
    }
}
