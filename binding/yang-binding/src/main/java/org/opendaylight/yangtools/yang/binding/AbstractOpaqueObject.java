/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility base class for {@link OpaqueObject} implementations. This class provides baseline implementation of
 * {@link #hashCode()} and {@link #equals(Object)} as specified by {@link OpaqueObject}.
 *
 * @param <T> Implemented OpaqueObject type
 */
@Beta
public abstract class AbstractOpaqueObject<T extends OpaqueObject<T>> implements OpaqueObject<T> {
    @Override
    public final int hashCode() {
        return 31 * implementedInterface().hashCode() + valueHashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpaqueObject)) {
            return false;
        }
        final OpaqueObject<?> other = (OpaqueObject<?>) obj;
        return implementedInterface().equals(other.implementedInterface()) && valueEquals(other.getValue());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)
            .add("implementedInterface", implementedInterface())).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("value", getValue());
    }

    protected boolean valueEquals(final @NonNull OpaqueData<?> otherValue) {
        return getValue().equals(otherValue);
    }

    protected int valueHashCode() {
        return getValue().hashCode();
    }
}
