/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 */
@NonNullByDefault
public interface DataTreeParent {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    Map<QName, ModeledData> asMap();

    default void forEach(final BiConsumer<QName, ? super ModeledData> action) {
        asMap().forEach(action);
    }

    default @Nullable ModeledData lookupNode(final QName nodeIdentifier) {
        return asMap().get(requireNonNull(nodeIdentifier));
    }
}
