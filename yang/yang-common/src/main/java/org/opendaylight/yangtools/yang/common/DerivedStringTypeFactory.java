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
 * Factory for {@link DerivedStringType}s.
 *
 * <T> derived string type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public interface DerivedStringTypeFactory<T extends DerivedStringType<T>> {
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
     * @return A {@link DerivedStringType} instance.
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str does not contain a valid representation
     */
    T forString(String str);

    /**
     * Create a instance for a string representation.
     *
     * @param str String representation
     * @return A {@link DerivedStringType} instance.
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
    default <X extends DerivedStringType<X>> DerivedStringTypeFactory<X> unsafe() {
        return (DerivedStringTypeFactory<X>) this;
    }
}
