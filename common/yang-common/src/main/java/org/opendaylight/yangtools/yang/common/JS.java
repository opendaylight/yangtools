/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record JS(String toRawString) implements JString {
    static final JS EMPTY = new JS("");

    JS {
        requireNonNull(toRawString);
    }

    @Override
    public int hashCode() {
        return toRawString.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Stringly other && toRawString.equals(other.toRawString());
    }

    @Override
    public String toString() {
        return toRawString;
    }
}
