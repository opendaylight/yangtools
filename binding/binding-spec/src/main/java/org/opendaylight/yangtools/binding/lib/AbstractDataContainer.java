/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Abstract base class for {@link DataContainer} implementations. It implements {@link #hashCode()} caching and common
 * handling of {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <T> the {@link DataContainer} type
 * @since 15.1.0
 */
public abstract class AbstractDataContainer<T extends DataContainer> implements JavaDataContainer<T> {
    // TODO: single field when hashCode() is defined to be != 0
    private int hashCode;
    private volatile boolean hashCodeValid;

    @Override
    public final int hashCode() {
        return hashCodeValid ? hashCode : loadHashCode();
    }

    private int loadHashCode() {
        final var result = bindingHashCode();
        hashCode = result;
        hashCodeValid = true;
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean equals(final Object obj) {
        // The unchecked cast here is safe, but we cannot quite express that. We are seeing implementedInterface()
        // as Class<? extends DataContainer> and we should be sharpening the return type to Class<T>. If we were to do
        // that, though, we would override the default method in the generated interface -- effectively forcing
        // subclasses to restore that wiring by:
        //
        //   public Class<Foo> implementedInterface() {
        //     return Foo.super.implementedInterface();
        //   }
        //
        // Which is exactly the bit wiring we want to side-step through the use of ImplementedInterface.
        //
        // So we rely on subclass to not try anything weird: it is bound by the class it returns from
        // implementedInterface(), so it better be giving us the correct contract :)
        return this == obj || implementedInterface().isInstance(obj) && bindingEquals((T) obj);
    }

    @Override
    public final String toString() {
        return bindingToString();
    }
}
