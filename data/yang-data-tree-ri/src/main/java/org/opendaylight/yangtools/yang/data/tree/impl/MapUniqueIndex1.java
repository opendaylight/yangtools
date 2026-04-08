/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * A {@link MapUniqueIndex} containing exactly one entry.
 */
@NonNullByDefault
record MapUniqueIndex1(Object uniqueValues, NodeIdentifierWithPredicates key) implements MapUniqueIndex {
    MapUniqueIndex1 {
        requireNonNull(uniqueValues);
        requireNonNull(key);
    }

    @Override
    public @Nullable NodeIdentifierWithPredicates lookupKey(final Object reqUniqueValues) {
        return Objects.deepEquals(uniqueValues, requireNonNull(reqUniqueValues)) ? key : null;
    }
}
