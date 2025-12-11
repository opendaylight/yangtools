/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Abstract base class for {@link ModelStatement} implementations. It mostly provides static methods for efficiently
 * storing lists.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 */
abstract sealed class AbstractModelStatement<A> implements ModelStatement<A>
        permits AbstractDeclaredStatement, AbstractEffectiveStatement {
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("argument", argument());
    }

    /**
     * Utility method for squashing singleton lists into single objects. This is a CPU/mem trade-off, which we are
     * usually willing to make: for the cost of an instanceof check we can save one object and re-create it when needed.
     * The inverse operation is #unmaskSubstatements(Object)}.
     *
     * @param list list to mask
     * @return Masked list
     * @throws NullPointerException if list is null
     */
    protected static final @NonNull Object maskList(final ImmutableList<?> list) {
        // Note: ImmutableList guarantees non-null content
        return list.size() == 1 ? list.getFirst() : list;
    }

    /**
     * Utility method for recovering singleton lists squashed by {@link AbstractModelStatement#maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if any argument is null
     * @throws ClassCastException if masked object does not match expected class
     */
    @SuppressWarnings("unchecked")
    protected static final <T> @NonNull ImmutableList<T> unmaskList(final @NonNull Object masked,
            final @NonNull Class<T> type) {
        return masked instanceof ImmutableList ? (ImmutableList<T>) masked
            // Yes, this is ugly code, which could use an explicit verify, that would just change the what sort
            // of exception we throw. ClassCastException is as good as VerifyException.
            : ImmutableList.of(type.cast(masked));
    }

    protected static final @NonNull Object maskSet(final ImmutableSet<?> set) {
        return set.size() == 1 ? set.iterator().next() : set;
    }

    protected static final <T> @NonNull ImmutableSet<T> unmaskSet(final @NonNull Object masked,
            final @NonNull Class<T> type) {
        return masked instanceof ImmutableSet ? (ImmutableSet<T>) masked
            // Yes, this is ugly code, which could use an explicit verify, that would just change the what sort
            // of exception we throw. ClassCastException is as good as VerifyException.
            : ImmutableSet.of(type.cast(masked));
    }
}
