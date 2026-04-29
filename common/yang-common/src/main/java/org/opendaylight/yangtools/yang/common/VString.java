/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link String} known to hold a validated value.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface VString extends Stringly permits CString, VS {
    /**
     * {@return a {@link VString} encapsulating specified value.
     * @param value the string value
     */
    static VString of(final String value) {
        return value.isEmpty() ? VS.EMPTY : new VS(value);
    }

    @Override
    default String toRawString() {
        return toValidString();
    }

    /**
     * {@return a valid String representation}
     */
    String toValidString();
}
