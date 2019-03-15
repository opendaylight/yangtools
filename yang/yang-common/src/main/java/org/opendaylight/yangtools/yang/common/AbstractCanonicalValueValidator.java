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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Abstract base class for implementing validators.
 *
 * @param <T> string representation class
 * @param <V> validated string representation class
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class AbstractCanonicalValueValidator<T extends DerivedString<T>, V extends T>
        implements CanonicalValueValidator<T, V> {
    private static final ClassValue<Boolean> IMPLEMENTATIONS = new AbstractCanonicalValueImplementationValidator() {
        @Override
        void checkCompareTo(final Class<?> type) {
            // Intentional no-op, as we'd need a type capture of the representation
        }
    };

    private final CanonicalValueSupport<T> representationSupport;
    private final Class<V> validatedClass;

    protected AbstractCanonicalValueValidator(final CanonicalValueSupport<T> representationSupport,
            final Class<V> validatedClass) {
        this.representationSupport = requireNonNull(representationSupport);
        IMPLEMENTATIONS.get(validatedClass);
        this.validatedClass = validatedClass;
    }

    @Override
    public final Class<T> getRepresentationClass() {
        return representationSupport.getRepresentationClass();
    }

    @Override
    public final Class<V> getValidatedRepresentationClass() {
        return validatedClass;
    }

    @Override
    public final Variant<T, CanonicalValueViolation> validateRepresentation(final T value) {
        return validatedClass.isAssignableFrom(value.validator().getValidatedRepresentationClass())
                ? Variant.ofFirst(validatedClass.cast(value)) : validate(value);
    }

    @Override
    public final Variant<T, CanonicalValueViolation> validateRepresentation(final T value,
            final String canonicalString) {
        return validatedClass.isAssignableFrom(value.validator().getValidatedRepresentationClass())
                ? Variant.ofFirst(validatedClass.cast(value)) : validate(value, requireNonNull(canonicalString));
    }

    /**
     * Validate a {@link DerivedString} representation. Subclasses should override this method if they can
     * provide a validation algorithm which does not rely on canonical strings but works on representation state only.
     *
     * @param value Representation value
     * @return Validated representation or CanonicalValueViolation
     * @throws NullPointerException if {@code value} is null
     */
    protected Variant<T, CanonicalValueViolation> validate(final T value) {
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
     */
    protected abstract Variant<T, CanonicalValueViolation> validate(T value, String canonicalString);
}
