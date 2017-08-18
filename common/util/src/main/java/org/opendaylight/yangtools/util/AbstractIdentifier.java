/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * An abstract {@link Identifier} backed by an immutable object. Subclasses have no control over {@link #hashCode()}
 * and {@link #equals(Object)}, hence they should not add any fields.
 *
 * @author Robert Varga
 *
 * @param <T> Object type
 */
public abstract class AbstractIdentifier<T> implements Identifier {
    private static final long serialVersionUID = 1L;

    private final T value;

    public AbstractIdentifier(final T value) {
        this.value = requireNonNull(value);
    }

    public final T getValue() {
        return value;
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        return getClass().equals(obj.getClass()) && value.equals(((AbstractIdentifier<?>)obj).value);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }
}
