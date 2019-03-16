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
 * Support for a {@link CanonicalValue} subclasses. An implementation of this interface must be registered
 * in the system and be available from each CanonicalValue object.
 *
 * <p>
 * Note: never implement this interface directly, subclass {@link AbstractCanonicalValueSupport} instead.
 *
 * <p>
 * This interface allows a {@link CanonicalValue} to be instantiated from a String. The implementation is expected
 * to perform all checks implied by the corresponding YANG data model.
 *
 * @param <T> canonical value type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface CanonicalValueSupport<T extends CanonicalValue<T>> extends CanonicalValueValidator<T, T> {
    /**
     * Create a instance for a string representation. Implementations of this method are required to perform checks
     * equivalent to the YANG data model restrictions attached to the corresponding YANG type. Non-canonical format
     * strings must be accepted and result in objects equal to objects obtained from the corresponding canonical format.
     *
     * @param str String representation
     * @return A {@link CanonicalValue} instance or CanonicalValueViolation if {@code str} does not conform
     * @throws NullPointerException if {@code str} is null
     */
    Variant<T, CanonicalValueViolation> fromString(String str);

    /**
     * Create a instance for the canonical string representation. Implementations of this method may perform
     * optimizations based on the assumption the string is canonical, but should still report errors when a mismatch
     * is detected.
     *
     * @param str String representation
     * @return A {@link CanonicalValue} instance or CanonicalValueViolation if {@code str} does not conform
     * @throws NullPointerException if {@code str} is null
     */
    default Variant<T, CanonicalValueViolation> fromCanonicalString(final String str) {
        return fromString(requireNonNull(str));
    }

    /**
     * Unsafe cast to a factory type.
     *
     * @return This instance cast to specified type
     */
    @SuppressWarnings("unchecked")
    default <X extends CanonicalValue<X>> CanonicalValueSupport<X> unsafe() {
        return (CanonicalValueSupport<X>) this;
    }
}
