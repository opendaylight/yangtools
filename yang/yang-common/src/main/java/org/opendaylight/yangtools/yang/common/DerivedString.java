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
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Abstract base class for objects which are string-equivalent to canonical string representation specified
 * in a YANG model. Note that each subclass of {@link DerivedString} defines its own {@link #hashCode()} and
 * {@link #equals(Object)} contracts. All subclasses are require to provide a non-identity implementations of these
 * methods.
 *
 * <T> derived string type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public abstract class DerivedString<T extends DerivedString<T>> implements Comparable<T>, Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Return the canonical string representation of this object's value.
     *
     * @return Canonical string
     */
    public abstract String canonicalString();

    /**
     * Return a {@link DerivedStringSupport} associated with this type.
     *
     * @return A support instance.
     */
    public abstract DerivedStringSupport<T> support();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return canonicalString();
    }
}
