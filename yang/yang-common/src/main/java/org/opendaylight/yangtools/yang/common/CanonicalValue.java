/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A typed value in its internal Java representation. Implementations of this interface are required to:
 * <ul>
 * <li>be immutable</li>
 * <li>be {@link Serializable}</li>
 * <li>accurately define total ordering of values</li>
 * </ul>
 *
 * <p>
 * Aside from providing the ability to hold a canonical value, this interface and its implementations support carrying
 * additional information about how the value has been validated -- allowing efficient interchange of already-validated
 * values between users. {@link #validator()} provides the link to a {@link CanonicalValueValidator} which has declared
 * the value conform to it. Users can query the validator to establish whether the value needs to be further validated
 * to conform to their requirement.
 *
 * @param <T> Canonical value type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface CanonicalValue<T extends CanonicalValue<T>> extends Comparable<T>, Immutable, Serializable {
    /**
     * Return the canonical string representation of this value.
     *
     * @return Canonical string
     */
    String toCanonicalString();

    /**
     * Return the {@link CanonicalValue} associated with this type. It can be used to create new instances of this
     * representation.
     *
     * @return A {@link CanonicalValue} instance.
     */
    CanonicalValueSupport<T> support();

    /**
     * Return a {@link CanonicalValueValidator} associated with this value's validated type.
     *
     * @return A {@link CanonicalValueValidator} instance.
     */
    default CanonicalValueValidator<T, ? extends T> validator() {
        return support();
    }
}
