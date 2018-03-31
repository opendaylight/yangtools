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
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Support for a {@link DerivedString} subclasses. An implementation of this interface must be registered
 * in the system and be available from each DerivedString object.
 *
 * <p>
 * Note: never implement this interface directly, subclass {@link AbstractDerivedStringSupport} instead.
 *
 * <p>
 * This interface allows a {@link DerivedString} to be instantiated from a String. The implementation is expected
 * to perform all checks implied by the corresponding YANG data model.
 *
 * @param <R> derived string representation
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public interface DerivedStringSupport<R extends DerivedString<R>> extends DerivedStringValidator<R, R> {
    /**
     * Create a instance for a string representation. Implementations of this method are required to perform checks
     * equivalent to the YANG data model restrictions attached to the corresponding YANG type. Non-canonical format
     * strings must be accepted and result in objects equal to objects obtained from the corresponding canonical format.
     *
     * @param str String representation
     * @return A {@link DerivedString} instance.
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str does not contain a valid representation
     */
    R fromString(String str);

    /**
     * Create a instance for the canonical string representation. Implementations of this method may perform
     * optimizations based on the assumption the string is canonical, but should still report errors when a mismatch
     * is detected.
     *
     * @param str String representation
     * @return A {@link DerivedString} instance.
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str does not contain canonical representation
     */
    default R fromCanonicalString(final String str) {
        return fromString(requireNonNull(str));
    }

    /**
     * Unsafe cast to a factory type.
     *
     * @return This instance cast to specified type
     */
    @SuppressWarnings("unchecked")
    default <X extends DerivedString<X>> DerivedStringSupport<X> unsafe() {
        return (DerivedStringSupport<X>) this;
    }
}
