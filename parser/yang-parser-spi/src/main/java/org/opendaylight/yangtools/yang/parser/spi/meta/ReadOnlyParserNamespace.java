/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

record ReadOnlyParserNamespace<K, V>(@NonNull String name) implements ParserNamespace.ReadOnly<K, V> {
    ReadOnlyParserNamespace {
        if (name.isBlank()) {
            throw new IllegalArgumentException("blank name");
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ParserNamespace.ReadOnly.class).add("name", name).toString();
    }
}
