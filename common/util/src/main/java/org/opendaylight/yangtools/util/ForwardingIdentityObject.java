/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Delegator;

/**
 * A {@link ForwardingObject} which additionally masks {@link #hashCode()}/{@link #equals(Object)} of a delegate object,
 * so that it can be a data transfer object with data-dependent implementations of those contracts can be use in
 * collections and maps which need to work on identity. This is useful in situations where identity equality needs to
 * be used with the conjunction with the collections library, for example {@link ConcurrentHashMap}.  All instances are
 * considered equal if they refer to the same delegate object.
 *
 * <p>
 * Note this class forms its own equality domain, and its use may lead to surprising results, especially where
 * {@link #toString()} is involved. For example a {@code Map.toString()} may end up emitting two keys which have the
 * same String representation.
 *
 * @param <T> Type of wrapped object
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class ForwardingIdentityObject<T> extends ForwardingObject implements Delegator<T> {
    protected ForwardingIdentityObject() {
        // Mask public constructor
    }

    public static <T> ForwardingIdentityObject<T> of(final T obj) {
        return checkedOf(requireNonNull(obj));
    }

    @Override
    public final @NonNull T getDelegate() {
        return delegate();
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(delegate());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof ForwardingIdentityObject
                && delegate() == ((ForwardingIdentityObject<?>) obj).delegate();
    }

    @Override
    protected abstract @NonNull T delegate();

    private static <T> ForwardingIdentityObject<T> checkedOf(final @NonNull T delegate) {
        return new ForwardingIdentityObject<>() {
            @Override
            protected @NonNull T delegate() {
                return delegate;
            }
        };
    }
}
