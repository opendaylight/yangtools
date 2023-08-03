/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FutureCallback;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A guarded value. The value is available through {@link #orElseThrow()} and {@link #orElseThrow(Function)} methods.
 * These force error handling to occur before an access is attempted.
 *
 * @param <V> Guarded value type
 */
@Beta
public abstract sealed class Try<V> {
    /**
     * A {@link Try} which does not have an active guard.
     *
     * @param <V> Value type
     * @param value The value
     */
    private static final class Success<V> extends Try<V> {
        private final V value;

        Success(final V value) {
            this.value = value;
        }

        @Override
        public <X extends RuntimeException> V orElseThrow(final Function<Throwable, X> supplier) {
            requireNonNull(supplier);
            return value;
        }
    }

    /**
     * A {@link Try} which has an active guard.
     *
     * @param <V> Value type
     * @param value The value
     */
    private static final class Failure<V> extends Try<V> {
        private final @NonNull Throwable cause;

        Failure(final Throwable cause) {
            this.cause = requireNonNull(cause);
        }

        @Override
        public <X extends RuntimeException> V orElseThrow(final Function<Throwable, X> supplier) throws X {
            throw supplier.apply(cause);
        }
    }

    private Try() {
        // Hidden on purpose
    }

    public static <V> @NonNull Try<V> of(final V value) {
        return new Success<>(value);
    }

    public static <V> @NonNull Try<V> failed(final Throwable cause) {
        return new Failure<>(cause);
    }

    /**
     * Return the tried value or transform the reported failure.
     *
     * @param <X> Thrown exception type
     * @param supplier Transformation of target exception
     * @return Resulting value
     * @throws X As supplied
     */
    public abstract <X extends RuntimeException> V orElseThrow(@NonNull Function<@NonNull Throwable, X> supplier)
        throws X;

    @SuppressWarnings("checkstyle:illegalThrows")
    public final V orElseThrow() {
        return orElseThrow(Try::sneakyThrow);
    }

    /**
     * This is useful in re-throwing {@link Throwable} without leaking that to the API contract, where we are
     * propagating something that might have been caught. There should not be a general use for this, but, for example,
     * {@link Future#get()} exposes its failure just as a Throwable. This in turn allows (bad) users to propagate
     * {@link Error}s through Futures, by simple catch-and-propagate. As a further example, Guava's
     * {@link FutureCallback} provides the failure cause, usually communicated through
     * {@link ExecutionException#getCause()} to be reported on failures.
     *
     * @param <T> Fake type of reported exception
     * @param <V> Fake type of returned value
     * @param throwable A {@link Throwable}
     * @return Never returns normally
     * @throws T always thrown
     * @see Try#orElseThrow()
     * @see <a href="https://projectlombok.org/features/SneakyThrows">@SneakyThrows</a>
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "very are being sneaky")
    private static <T extends RuntimeException, V> V sneakyThrow(final Throwable throwable) throws T {
        throw (T) throwable;
    }
}