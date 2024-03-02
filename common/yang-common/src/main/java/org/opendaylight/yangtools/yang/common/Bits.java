/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@code bits} value.
 */
@NonNullByDefault
public interface Bits extends Immutable, Serializable, Set<String> {
    record Bit(String name, Uint32 position) implements Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public Bit {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Empty bit name");
            }
            requireNonNull(position);
        }

        @java.io.Serial
        Object writeReplace() {

        }
    }

    default boolean isSet(final Uint32 position) {
        return isSet(position.longValue());
    }

    boolean isSet(long position);

    boolean isSet(String name);

    Set<Bit> setBits();
}
