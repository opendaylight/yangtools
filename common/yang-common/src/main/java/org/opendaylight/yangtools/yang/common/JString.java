/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * A raw {@link String}.
 *
 * @since 16.0.0
 */
public sealed interface JString extends Stringly permits JS {
    /**
     * {@return a {@link JString} encapsulating specified raw string.
     * @param rawString raw string
     */
    static JString of(final String rawString) {
        return rawString.isEmpty() ? JS.EMPTY : new JS(rawString);
    }
}
