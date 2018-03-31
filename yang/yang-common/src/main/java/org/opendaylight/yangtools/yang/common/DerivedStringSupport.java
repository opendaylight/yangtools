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
 * Support for a {@link DerivedString} specialization. An implementation of this interface must be registered
 * in the system and be available from each DerivedString object.
 *
 * @param <T> derived string type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public interface DerivedStringSupport<T extends DerivedString<T>> {
    /**
     * Returns the instantiated representation class.
     *
     * @return Instantiated representation class.
     */
    Class<T> getRepresentationClass();

    /**
     * Create a instance for a string representation.
     *
     * @param str String representation
     * @return A {@link DerivedString} instance.
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str does not contain a valid representation
     */
    T forString(String str);

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
    default T forCanonicalString(final String str) {
        return forString(requireNonNull(str));
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
