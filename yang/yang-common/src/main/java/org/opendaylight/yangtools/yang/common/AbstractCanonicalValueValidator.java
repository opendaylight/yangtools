/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for implementing validators.
 *
 * @param <R> string representation class
 * @param <T> validated string representation class
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public abstract class AbstractCanonicalValueValidator<R extends DerivedString<R>, T extends R>
        implements CanonicalValueValidator<R, T> {
    private static final ClassValue<Boolean> IMPLEMENTATIONS = new AbstractCanonicalValueImplementationValidator() {
        @Override
        void checkCompareTo(@NonNull final Class<?> type) {
            // Intentional no-op, as we'd need a type capture of the representation
        }
    };

    private final CanonicalValueSupport<R> representationSupport;
    private final Class<T> validatedClass;

    protected AbstractCanonicalValueValidator(final CanonicalValueSupport<R> representationSupport,
            final Class<T> validatedClass) {
        this.representationSupport = requireNonNull(representationSupport);
        IMPLEMENTATIONS.get(validatedClass);
        this.validatedClass = validatedClass;
    }

    @Override
    public final Class<R> getRepresentationClass() {
        return representationSupport.getRepresentationClass();
    }

    @Override
    public final Class<T> getValidatedRepresentationClass() {
        return validatedClass;
    }

    @Override
    public final T validateRepresentation(final R value) {
        @Nullable T valid;
        return (valid = castIfValid(value)) != null ? valid : validate(value);
    }

    @Override
    public final T validateRepresentation(final R value, final String canonicalString) {
        @Nullable T valid;
        return (valid = castIfValid(value)) != null ? valid : validate(value, requireNonNull(canonicalString));
    }

    /**
     * Validate a {@link DerivedString} representation. Subclasses should override this method if they can
     * provide a validation algorithm which does not rely on canonical strings but works on representation state only.
     *
     * @param value Representation value
     * @return Validated representation
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if the value does not meet validation criteria.
     */
    protected T validate(final R value) {
        return validate(value, value.toCanonicalString());
    }

    /**
     * Validate a {@link DerivedString} representation. Subclasses can chose whether they operate on representation
     * state or canonical string -- both are considered equivalent.
     *
     * @param value Representation value
     * @param canonicalString Canonical string matching the representation value
     * @return Validated representation
     * @throws NullPointerException if {@code value} or {@code canonicalString} is null.
     * @throws IllegalArgumentException if the value does not meet validation criteria.
     */
    protected abstract T validate(R value, String canonicalString);

    private @Nullable T castIfValid(final R value) {
        return validatedClass.isAssignableFrom(value.validator().getValidatedRepresentationClass())
                ? validatedClass.cast(value) : null;
    }
}
