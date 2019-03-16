/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * {@link CanonicalValue} validator interface. Implementations of this interface can perform further validation of
 * representation state such that it conforms to a YANG type derived from a type with a {@link CanonicalValue}
 * representation.
 *
 * <p>
 * Note: this interface should not be directly implemented. Use {@link AbstractCanonicalValueValidator} instead.
 *
 * @param <T> canonical value type
 * @param <V> validated canonical value type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface CanonicalValueValidator<T extends CanonicalValue<T>, V extends T> extends Immutable {
    /**
     * Returns the instantiated representation class. The representation class is a {@link CanonicalValue} which
     * understands the semantics of modeled data and has some internal representation of it. All {@link CanonicalValue}s
     * which share the same representation class are considered equal if their internal state would result in the
     * same canonical string representation as defined by the YANG data model.
     *
     * @return Representation {@link CanonicalValue} class.
     */
    Class<T> getRepresentationClass();

    /**
     * Return the class which captures the fact it was validated by this validator.
     *
     * @return Validated capture of the representation class.
     */
    Class<V> getValidatedRepresentationClass();

    /**
     * Validate a {@link CanonicalValue} representation. Implementations should override this method if they can
     * provide a validation algorithm which does not rely on canonical strings but works on representation state only.
     *
     * @param value Representation value
     * @return Validated representation or a {@link CanonicalValueViolation}
     * @throws NullPointerException if {@code value} is null
     */
    default Variant<T, CanonicalValueViolation> validateRepresentation(final T value) {
        return validateRepresentation(value, value.toCanonicalString());
    }

    /**
     * Validate a {@link CanonicalValue} representation. Implementations can chose whether they operate on
     * representation state or canonical string -- both are considered equivalent. Users should call this method if they
     * have a representation readily available.
     *
     * @param value Representation value
     * @param canonicalString Canonical string matching the representation value
     * @return Validated representation or a {@link CanonicalValueViolation}
     * @throws NullPointerException if {@code value} or {@code canonicalString} is null.
     */
    Variant<T, CanonicalValueViolation> validateRepresentation(T value, String canonicalString);
}
