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

/**
 * YANG string representation validator. Implementations of this interface can perform further validation of
 * representation state such that it conforms to a YANG string type derived from a type with a {@link DerivedString}
 * representation class.
 *
 * <p>
 * Note: this interface should not be directly implemented. Use {@link AbstractDerivedStringValidator} instead.
 *
 * @param <R> string representation class
 * @param <T> validated string representation class
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface DerivedStringValidator<R extends DerivedString<R>, T extends R> {
    /**
     * Returns the instantiated representation class. The representation class is a {@link DerivedString} which
     * understands the semantics of modeled data and has some internal representation of it. All {@link DerivedString}s
     * which share the same representation class are considered equal if their internal state would result in the
     * same canonical string representation as defined by the YANG data model.
     *
     * @return Representation {@link DerivedString} class.
     */
    Class<R> getRepresentationClass();

    /**
     * Return the class which captures the fact it was validated by this validator.
     *
     * @return Validated capture of the representation class.
     */
    Class<T> getValidatedRepresentationClass();

    /**
     * Validate a {@link DerivedString} representation. Implementations should override this method if they can
     * provide a validation algorithm which does not rely on canonical strings but works on representation state only.
     *
     * @param value Representation value
     * @return Validated representation
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if the value does not meet validation criteria.
     */
    default T validateRepresentation(final R value) {
        return validateRepresentation(value, value.toCanonicalString());
    }

    /**
     * Validate a {@link DerivedString} representation. Implementations can chose whether they operate on representation
     * state or canonical string -- both are considered equivalent. Users should call this method if they have
     * a representation readily available.
     *
     * @param value Representation value
     * @param canonicalString Canonical string matching the representation value
     * @return Validated representation
     * @throws NullPointerException if {@code value} or {@code canonicalString} is null.
     * @throws IllegalArgumentException if the value does not meet validation criteria.
     */
    T validateRepresentation(R value, String canonicalString);
}
