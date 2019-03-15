/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility holder similar to {@link java.util.Optional}, except the empty case contains an Exception, which should be
 * reported, for example via throwing it. It provides analogous methods such as {@link #isPresent()},
 * {@link #ifPresent(Consumer)}, {@link #get()}, {@link #orElse(Object)}, {@link #orElseGet(Supplier)},
 * {@link #orElseThrow(Function)}.
 *
 * @param <T> Value type
 * @param <E> Exception type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class CheckedValue<T, E extends Exception> extends Variant<T, E> {
    protected CheckedValue(final T value) {
        super(value);
    }

    protected CheckedValue(final E violation, final @Nullable Void dummy) {
        super(violation, dummy);
    }

    /**
     * Create a new instance containing an {@link Exception}.
     *
     * @param cause Throwable
     * @param <T> Value type
     * @param <E> Exception type
     * @return A new instance
     * @throws NullPointerException if {@code cause} is null
     */
    public static <T, E extends Exception> CheckedValue<T, E> ofException(final E cause) {
        return new CheckedValue<>(cause, null);
    }

    /**
     * Create a new instance containing specified value.
     *
     * @param value Value
     * @param <T> Value type
     * @param <E> Exception type
     * @return A new instance
     * @throws NullPointerException if {@code value} is null
     */
    public static <T, E extends Exception> CheckedValue<T, E> ofValue(final T value) {
        return new CheckedValue<>(value);
    }

    /**
     * Convert a Variant into a {@link CheckedValue}, converting the second value into an exception.
     *
     * @param variant Input variant
     * @param mapper Mapping function from second alternative to an exception
     * @param <T> First alternative type
     * @param <U> Second alternative type
     * @param <E> Exception type
     * @return Resulting {@link CheckedValue}
     */
    public static <T, U, E extends Exception> CheckedValue<T, E> ofVariant(final Variant<T, U> variant,
            final Function<U, E> mapper) {
        requireNonNull(mapper);
        return variant.isFirst() ? new CheckedValue<>(variant.first())
                : new CheckedValue<>(mapper.apply(variant.second()), null);
    }

    /**
     * Return the contained value if {@link #isPresent()} would return true, throws {@link IllegalStateException}
     * otherwise.
     *
     * @return Contained value
     * @throws IllegalStateException if an error string is present.
     */
    public final T get() {
        if (isFirst()) {
            return first();
        }
        throw new IllegalStateException("Value is not present", second());
    }

    /**
     * Return the contained error string if {@link #isPresent()} would return false, throws
     * {@link IllegalStateException} otherwise.
     *
     * @return Throwable which was used to instantiate this object, or absent if it was instantiated using an error
     *         string.
     * @throws IllegalStateException if a value is present.
     */
    public final E getException() {
        if (isSecond()) {
            return second();
        }
        throw new IllegalStateException("Value " + first() + " is present");
    }

    /**
     * Return true if a value is present.
     *
     * @return True if a value is present.
     */
    public final boolean isPresent() {
        return isFirst();
    }

    /**
     * If a value is present, invoke the specified consumer with the value, otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    public final void ifPresent(final Consumer<? super T> consumer) {
        if (isFirst()) {
            consumer.accept(first());
        }
    }

    @SuppressWarnings("unchecked")
    public <U> CheckedValue<U, E> map(final Function<? super T, U> mapper) {
        requireNonNull(mapper);
        return isFirst() ? new CheckedValue<>(mapper.apply(first())) : (CheckedValue<U, E>) this;
    }

    @SuppressWarnings("unchecked")
    public <X extends Exception> CheckedValue<T, X> mapException(final Function<? super E, X> mapper) {
        requireNonNull(mapper);
        if (isFirst()) {
            return (CheckedValue<T, X>) this;
        }
        return new CheckedValue<>(mapper.apply(second()), null);
    }


    @SuppressWarnings("unchecked")
    public <U> CheckedValue<U, E> flatMap(final Function<? super T, CheckedValue<U, E>> mapper) {
        requireNonNull(mapper);
        return isFirst() ? requireNonNull(mapper.apply(first())) : (CheckedValue<U, E>) this;
    }

    /**
     * Return contained value if present, otherwise return supplied value.
     *
     * @param other Replacement value
     * @return Contained value or {code other}
     */
    public final T orElse(final T other) {
        return isFirst() ? first() : other;
    }

    /**
     * Return contained value if present, otherwise return the value produced by a supplier.
     *
     * @param supplier Replacement value supplier
     * @return Contained value or supplier's value
     * @throws NullPointerException if {@code supplier} is null
     */
    public final T orElseGet(final Supplier<T> supplier) {
        requireNonNull(supplier);
        return isFirst() ? first() : supplier.get();
    }

    /**
     * Return contained value if present or throw the exception alternative.
     *
     * @return Contained value
     * @throws E When there is no contained value
     */
    public final T orElseThrow() throws E {
        if (isFirst()) {
            return first();
        }
        throw second();
    }

    /**
     * Return contained value if present or throw the exception alternative mapped through provided mapper.
     *
     * @param exceptionMapper Exception mapper
     * @param <X> Thrown exception type
     * @return Contained value
     * @throws NullPointerException if {@code exceptionMapper} is null
     * @throws X When there is no contained value
     */
    public final <X extends Throwable> T orElseThrow(final Function<E, X> exceptionMapper) throws X {
        requireNonNull(exceptionMapper);
        if (isFirst()) {
            return first();
        }
        throw exceptionMapper.apply(second());
    }

    /**
     * Return contained value if present or throw the exception supplied by supplier.
     *
     * @param supplier Exception supplier
     * @param <X> Thrown exception type
     * @return Contained value
     * @throws NullPointerException if {@code exceptionMapper} is null
     * @throws X When there is no contained value
     */
    public final <X extends Throwable> T orElseThrow(final Supplier<X> supplier) throws X {
        requireNonNull(supplier);
        if (isFirst()) {
            return first();
        }
        throw supplier.get();
    }

    /**
     * Complete target {@link CompletableFuture} either successfully or exceptionally based on the state of this object.
     *
     * @param future Future to complete
     * @return True if this call has transitioned the future to a completed state, false otherwise.
     * @throws NullPointerException if {code future} is null
     */
    public final boolean completeFuture(final CompletableFuture<T> future) {
        return isFirst() ? future.complete(first()) : future.completeExceptionally(second());
    }

    /**
     * Complete target {@link SettableFuture} either successfully or exceptionally based on the state of this object.
     *
     * @param future Future to complete
     * @return True if this call has transitioned the future to a completed state, false otherwise.
     * @throws NullPointerException if {code future} is null
     */
    public final boolean completeFuture(final SettableFuture<T> future) {
        return isFirst() ? future.set(first()) : future.setException(second());
    }

    /**
     * Transform this object into an immediately-completed {@link CompletableFuture}. The future will be successful
     * if this object has a contained value or unsuccessful if this objects contains an exception.
     *
     * @return A {@link CompletableFuture}.
     */
    public final CompletableFuture<T> toCompletableFuture() {
        if (isFirst()) {
            return CompletableFuture.completedFuture(first());
        }
        // FIXME: Java 9: use CompletableFuture.failedFuture()
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(second());
        return future;
    }

    /**
     * Transform this object into an immediately-completed {@link FluentFuture}. The future will be successful
     * if this object has a contained value or unsuccessful if this objects contains an exception.
     *
     * @return A {@link FluentFuture}.
     */
    public final FluentFuture<T> toFluentFuture() {
        final ListenableFuture<T> future;
        if (isFirst()) {
            future = Futures.immediateFuture(first());
        } else {
            future = Futures.immediateFailedFuture(second());
        }
        return FluentFuture.from(future);
    }
}
