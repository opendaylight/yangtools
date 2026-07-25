/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Support methods for {@link ListField} implementations.
 */
@NonNullByDefault
final class ListFieldSupport {
    private ListFieldSupport() {
        // hidden on purpose
    }

    static <V> Object maskList(final Class<V> val, final List<V> list) {
        return switch (list.size()) {
            case 0 -> List.of();
            case 1 -> val.cast(requireNonNull(list.getFirst()));
            default -> List.copyOf(list);
        };
    }

    static <V> List<V> unmaskList(final Class<V> val, final Object obj) {
        return switch (obj) {
            case List<?> list -> {
                @SuppressWarnings("unchecked")
                final var cast = (List<V>) list;
                yield cast;
            }
            default -> List.of(val.cast(obj));
        };
    }
}
