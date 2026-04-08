/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * A {@link MapUniqueIndex} containing multiple entries.
 */
@NonNullByDefault
record MapUniqueIndexN(Map<Object, NodeIdentifierWithPredicates> map) implements MapUniqueIndex {
    /**
     * Singleton instance holding no entries.
     */
    static final MapUniqueIndex EMPTY = new MapUniqueIndexN(Map.of());

    MapUniqueIndexN {
        requireNonNull(map);
    }

    @Override
    public @Nullable NodeIdentifierWithPredicates lookupKey(final Object uniqueValues) {
        return map.get(requireNonNull(BinaryValue.wrap(uniqueValues)));
    }
}