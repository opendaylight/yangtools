/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility holder of a two-variant value. The class design treats both variants as equal.
 *
 * @param <T> First alternative type
 * @param <U> Second alternative type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public class Variant<T, U> {
    private final @Nullable T first;
    private final @Nullable U second;

    Variant(final T first) {
        this.first = requireNonNull(first);
        second = null;
    }

    Variant(final U second, final @Nullable Void dummy) {
        first = null;
        this.second = requireNonNull(second);
    }

    final T first() {
        return verifyNotNull(first);
    }

    final U second() {
        return verifyNotNull(second);
    }

    /**
     * Create a new instance containing specified value.
     *
     * @param value Value
     * @return A new instance
     * @throws NullPointerException if {@code value} is null
     */
    public static <T, U> Variant<T, U> ofFirst(final T value) {
        return new Variant<>(value);
    }

    /**
     * Create a new instance containing specified value.
     *
     * @param value Value
     * @return A new instance
     * @throws NullPointerException if {@code value} is null
     */
    public static <T, U> Variant<T, U> ofSecond(final U value) {
        return new Variant<>(value, null);
    }

    public final boolean isFirst() {
        return first != null;
    }

    public final T getFirst() {
        return tryFirst().get();
    }

    public final Optional<T> tryFirst() {
        return Optional.ofNullable(first);
    }

    public final boolean isSecond() {
        return second != null;
    }

    public final U getSecond() {
        return trySecond().get();
    }

    public final Optional<U> trySecond() {
        return Optional.ofNullable(second);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        final Variant<?, ?> other = (Variant<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public final String toString() {
        return addToString(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToString(final ToStringHelper helper) {
        return helper.add("first", first).add("second", second);
    }
}
