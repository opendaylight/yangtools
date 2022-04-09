/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Marker interface for specifying ordering of items. There are two orderings, {@link Ordered} and {@link Unordered},
 * and they are mutually exclusive. Item ordering is considered semantically important, such as it impacts
 * {@link Object#equals(Object)} contract. This can be thought of as the missing interface distinction between
 * {@link Collection}, {@link List} and {@link Set}.
 *
 * @param <T> Item order type
 */
@Beta
public sealed interface ItemOrder<T extends ItemOrder<T>> {
    /**
     * Items are ordered and their order is significant. A {@link List} is an example of a collection which conforms to
     * this contract.
     */
    non-sealed interface Ordered extends ItemOrder<Ordered> {
        @Override
        default Class<Ordered> itemOrder() {
            return Ordered.class;
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Hash code contract of {@link Ordered} objects <b>should</b> be sensitive to item order. In general similar to
         * {@link List#hashCode()} (in the {@code must} reading of sensitivity. {@code need not} reading of sensitivity
         * could also be implemented as {@code Map.hashCode()} in case of a map-like container.
         */
        // FIXME: 8.0.0: tighten 'should' to 'must'?
        @Override
        int hashCode();

        /**
         * {@inheritDoc}
         *
         * <p>
         * Equality contract of {@link Ordered} objects <b>must</b> be sensitive to item order, similar to
         * {@link List#equals(Object)}.
         */
        @Override
        boolean equals(Object obj);
    }

    /**
     * Items are unordered and their order is insignificant. A {@link Set} is an example of a collection which conforms
     * to this contract.
     */
    non-sealed interface Unordered extends ItemOrder<Unordered> {
        @Override
        default Class<Unordered> itemOrder() {
            return Unordered.class;
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * Hash code contract of {@link Unordered} objects <b>must</b> be insensitive to item order, similar to
         * {@link Set#hashCode()}.
         *
         * <p>
         * This contract is also exposed through {@link #itemOrder()}.
         */
        @Override
        int hashCode();

        /**
         * {@inheritDoc}
         *
         * <p>
         * Equality contract of {@link Unordered} objects <b>must</b> be insensitive to item order, similar to
         * {@link Set#equals(Object)}.
         *
         * <p>
         * This contract is also exposed through {@link #itemOrder()}.
         */
        @Override
        boolean equals(Object obj);
    }

    /**
     * Return the item order class of this object. The class' equality contracts apply to this object's equality
     * contract.
     *
     * @return Item order class.
     */
    @NonNull Class<T> itemOrder();

    /**
     * {@link ItemOrder} has impact on {@link #hashCode()}.
     */
    @Override
    int hashCode();

    /**
     * {@link ItemOrder} has impact on {@link #equals(Object)}.
     */
    @Override
    boolean equals(@Nullable Object obj);
}
