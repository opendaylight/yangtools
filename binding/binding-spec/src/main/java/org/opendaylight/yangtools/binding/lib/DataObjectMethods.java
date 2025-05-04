/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * Reference implementation of {@link Object#hashCode()}, {@link Object#equals(Object)} and {@link Object#toString()}
 * for a particular {@link DataObject} type. These would normally be part of the type, but {@link DataObject} types are
 * interfaces and therefore cannot provide them directly.
 */
// Note: we really would want to call this 'ObjectMethods', but that name already exists in java.lang.runtime.
@NonNullByDefault
public abstract class DataObjectMethods<T extends DataObject> {
    private final Class<T> typeClass;

    protected DataObjectMethods(final Class<T> typeClass) {
        this.typeClass = requireNonNull(typeClass);
    }

    /**
     * Default implementation of {@link Object#hashCode()} contract for a {@link DataObject}.
     *
     * @param thisObj Object acting as the receiver of hashCode invocation
     * @return hash code value of data modeled by the DataObject
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public final int bindingHashCode(final T thisObj) {
        return hashProperties(requireNonNull(thisObj));
    }

    /**
     * Default implementation of {@link Object#equals(Object)} contract for a {@link DataObject}.
     *
     * @param thisObj Object acting as the receiver of equals invocation
     * @param obj Object acting as argument to equals invocation
     * @return {@code true} if {@code thisObj} and {@code obj} are considered equal
     * @throws NullPointerException if {@code thisObj} is {@code null}
     */
    public final boolean bindingEquals(final T thisObj, final @Nullable Object obj) {
        return requireNonNull(thisObj) == obj || obj instanceof DataObject dataObject
            && typeClass.equals(dataObject.implementedInterface()) && equalProperties(thisObj, typeClass.cast(obj));
    }

    /**
     * Default implementation of {@link Object#toString()} contract for a {@link DataObject}.
     *
     * @param thisObj Object for which to generate toString() result
     * @return {@link String} value of data modeled by the DataObject
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public final String bindingToString(final T thisObj) {
        final var helper = MoreObjects.toStringHelper(typeClass);
        appendProperties(requireNonNull(thisObj), helper);
        return helper.toString();
    }

    /**
     * Returns the hash code calculated from properties of a {@link DataObject}.
     *
     * @param thisObj Object acting as the receiver of hashCode invocation
     * @return hash code value of data modeled by the DataObject
     */
    protected abstract int hashProperties(T thisObj);

    /**
     * Returns true if two object's properties are equal.
     *
     * @param thisObj Object acting as the receiver of hashCode invocation
     * @param thatObj Object acting as argument to equals invocation
     * @return {@code true} if and only if the objects' properties are equal
     */
    protected abstract boolean equalProperties(T thisObj, T thatObj);

    protected abstract void appendProperties(T thisObj, ToStringHelper helper);
}
