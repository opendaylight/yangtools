/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Serial;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated singleton type for YANG's 'type empty' value.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class Empty implements Immutable, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Empty INSTANCE = new Empty();

    private Empty() {
        // Hidden on purpose
    }

    /**
     * Return the singleton {@link Empty} value.
     *
     * @return Empty value.
     */
    public static Empty value()  {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "empty";
    }

    @Serial
    @SuppressWarnings("static-method")
    private Object readResolve() {
        return INSTANCE;
    }
}
