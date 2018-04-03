/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility holder similar to {@link java.util.Optional}, except the empty case contains an error string or a Throwable,
 * which should be reported, for example via an exception. It provides analogous methods such as {@link #isPresent()},
 * {@link #ifPresent(Consumer)}, {@link #get()}, {@link #orElse(Object)}, {@link #orElseGet(Supplier)},
 * {@link #orElseThrow(Function)}.
 *
 * @param <T> Value type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public final class CheckedValue<T> {
    private final @Nullable Object error;
    private final @Nullable T value;

    private CheckedValue(final T value) {
        this.value = requireNonNull(value);
        error = null;
    }

    private CheckedValue(final Object error, final @Nullable Void dummy) {
        this.error = requireNonNull(error);
        value = null;
    }

    /**
     * Create a new instance containing specified error string.
     *
     * @param errorString Error string
     * @return A new instance
     * @throws NullPointerException if {@code errorString} is null
     */
    public static <T> CheckedValue<T> ofErrorString(final String errorString) {
        return new CheckedValue<>(errorString, null);
    }

    /**
     * Create a new instance containing a {@link Throwable}.
     *
     * @param cause Throwable
     * @return A new instance
     * @throws NullPointerException if {@code cause} is null
     */
    public static <T> CheckedValue<T> ofThrowable(final Throwable cause) {
        return new CheckedValue<>(cause, null);
    }

    /**
     * Create a new instance containing specified value.
     *
     * @param value Value
     * @return A new instance
     * @throws NullPointerException if {@code value} is null
     */
    public static <T> CheckedValue<T> ofValue(final T value) {
        return new CheckedValue<>(value);
    }

    /**
     * Return the contained value if {@link #isPresent()} would return true, throws {@link IllegalStateException}
     * otherwise.
     *
     * @return Contained value
     * @throws IllegalStateException if an error string is present.
     */
    public T get() {
        if (value != null) {
            return value;
        }
        throw error instanceof String ? new IllegalStateException((String) error)
                : new IllegalStateException("Value is not present", getCause());
    }

    /**
     * Return the contained error string if {@link #isPresent()} would return false, throws
     * {@link IllegalStateException} otherwise.
     *
     * @return Error string or empty if this object was instantiated using {@link #ofThrowable(Throwable)}.
     * @throws IllegalStateException if a value is present.
     */
    public Optional<String> getErrorString() {
        checkNoValue();
        return error instanceof String ? Optional.of((@NonNull String) error) : Optional.empty();
    }

    /**
     * Return the contained error string if {@link #isPresent()} would return false, throws
     * {@link IllegalStateException} otherwise.
     *
     * @return Throwable which was used to instantiate this object, or absent if it was instantiated using an error
     *         string.
     * @throws IllegalStateException if a value is present.
     */
    public Optional<Throwable> getThrowable() {
        checkNoValue();
        return error instanceof Throwable ? Optional.of((@NonNull Throwable) error) : Optional.empty();
    }

    /**
     * Return true if a value is present.
     *
     * @return True if a value is present.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value, otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    public void ifPresent(final Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    @SuppressWarnings("unchecked")
    public <U> CheckedValue<U> map(final Function<? super T, U> mapper) {
        requireNonNull(mapper);
        return value != null ? new CheckedValue<>(mapper.apply(value)) : (CheckedValue<U>) this;
    }

    @SuppressWarnings("unchecked")
    public <U> CheckedValue<U> flatMap(final Function<? super T, CheckedValue<U>> mapper) {
        requireNonNull(mapper);
        return value != null ? requireNonNull(mapper.apply(value)) : (CheckedValue<U>) this;
    }

    /**
     * Return contained value if present, otherwise return supplied value.
     *
     * @param other Replacement value
     * @return Contained value or {code other}
     */
    public T orElse(final T other) {
        return value != null ? value : other;
    }

    /**
     * Return contained value if present, otherwise return the value produced by a supplier.
     *
     * @param supplier Replacement value supplier
     * @return Contained value or supplier's value
     * @throws NullPointerException if {@code supplier} is null
     */
    public T orElseGet(final Supplier<T> supplier) {
        requireNonNull(supplier);
        return value != null ? value : supplier.get();
    }

    public <X extends Throwable> T orElseThrow(final Function<@Nullable String, X> exceptionMapper) throws X {
        requireNonNull(exceptionMapper);
        if (value != null) {
            return value;
        }
        if (error instanceof String) {
            throw exceptionMapper.apply((String) error);
        }
        final Throwable cause = getCause();
        final X exception = exceptionMapper.apply(cause.getMessage());
        exception.addSuppressed(cause);
        throw exception;
    }

    @Override
    public int hashCode() {
        return MoreObjects.firstNonNull(value, error).hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CheckedValue)) {
            return false;
        }
        final CheckedValue<?> other = (CheckedValue<?>) obj;
        return Objects.equals(value, other.value) && Objects.equals(error, other.error);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("value", value).add("error", error).toString();
    }

    private void checkNoValue() {
        if (value != null) {
            throw new IllegalStateException("Value " + value + " is present");
        }
    }

    private Throwable getCause() {
        verify(error instanceof Throwable);
        return (@NonNull Throwable) error;
    }
}
