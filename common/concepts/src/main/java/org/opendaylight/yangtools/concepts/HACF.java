/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.base.Verify;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Convenience static methods for detecting
 * <a href="https://en.wikipedia.org/wiki/Halt_and_Catch_Fire_(computing)">Halt and Catch Fire</a> conditions. Provided
 * methods are taking a positive attitude towards implying errors:
 * <ul>
 *   <li>we do not {@link Objects#requireNonNull(Object)}, we are catching on fire {@link #hacfIfNull(Object)}</li>
 *   <li>
 *
 * </ul>
 *
 * @since 16.2.0
 * @see HACFException
 * @see Verify
 */
public final class HACF {
    private HACF() {
        // hidden on purpose
    }

    /**
     * HACF with specified message if a condition is {@code true}.
     *
     * @param expression the condition
     * @param message the error message
     * @throws HACFException if {@code expression} is {@code true}
     * @see HACFException
     */
    @NonNullByDefault
    public static void hacfIf(final boolean expression, final String message) {
        if (expression) {
            throw new HACFException(message);
        }
    }

    /**
     * HACF with a message obtained from a supplier if a condition is {@code true}.
     *
     * @param expression the condition
     * @param message the error message
     * @throws HACFException if {@code expression} is {@code true}
     */
    @NonNullByDefault
    public static void hacfIf(final boolean expression, final Supplier<String> supplier) {
        if (expression) {
            throw new HACFException(supplier.get());
        }
    }

    /**
     * HACF with a {@link String#formatted(Object...)} message if a condition is {@code true}.
     *
     * @param expression the condition
     * @param format the format string
     * @param args format arguments
     * @throws HACFException if {@code expression} is {@code true}
     * @see HACFException
     */
    @FormatMethod
    @NonNullByDefault
    public static void hacfIf(final boolean expression, final @FormatString String format,
            final Object @Nullable ... args) {
        if (expression) {
            throw new HACFException(format.formatted(args));
        }
    }

    /**
     * HACF if specified reference is {@code null}.
     *
     * @param <T> object type
     * @param reference the object
     * @return a non-null reference
     * @throws HACFException if {@code obj} is {@code null}
     * @see Verify#verifyNotNull(Object)
     */
    public static <T> @NonNull T hacfIfNull(final @Nullable T reference) {
        if (reference == null) {
            throw new HACFException("unexpected null reference");
        }
        return reference;
    }
}
