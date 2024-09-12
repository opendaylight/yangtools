/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Serialization proxy for {@link YangBoolean}.
 */
@NonNullByDefault
record Bv1(boolean value) implements Serializable {
    static final Bv1 TRUE = new Bv1(true);
    static final Bv1 FALSE = new Bv1(false);

    @java.io.Serial
    private Object readResolve() {
        return YangBoolean.valueOf(value);
    }
}
