/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ItemOrder.Ordered;
import org.opendaylight.yangtools.concepts.ItemOrder.Unordered;
import org.opendaylight.yangtools.yang.common.Ordering;

/**
 * Marker interfaces for {@link NormalizedNodeContainer}s which have distinct ordering requirements.
 */
@NonNullByDefault
public sealed interface OrderingAware permits NormalizedContainer, OrderingAware.System, OrderingAware.User {
    /**
     * Marker interface for NormalizedNodeContainer implementations which correspond to {@code ordered-by system}. These
     * follow the {@link Unordered} contract.
     */
    non-sealed interface System extends OrderingAware, Unordered {
        @Override
        default Ordering ordering() {
            return Ordering.SYSTEM;
        }
    }

    /**
     * Marker interface for NormalizedNodeContainer implementations which correspond to {@code ordered-by user}. These
     * follow the {@link Ordered} contract.
     */
    non-sealed interface User extends OrderingAware, Ordered {
        @Override
        default Ordering ordering() {
            return Ordering.USER;
        }
    }

    /**
     * Ordering items within this object.
     *
     * @return This object's item ordering.
     */
    Ordering ordering();
}
