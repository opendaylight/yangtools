/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BindingContract;

/**
 * A mixin interface providing canonical implementations of {@link #hashCode()}, {@link #equals(Object)} and
 * {@link #toString()} where that contract is defined for a particular {@link BindingContract}.
 *
 * @param B base {@link BindingContract} type
 * @param T concrete type
 * @since 16.0.0
 */
public sealed interface JavaContract<B extends BindingContract<B>, T extends B> extends ImplementedInterface<B>
        permits JavaDataContainer {
    @Override
    int hashCode();

    int bindingHashCode();

    @Override
    boolean equals(Object obj);

    /**
     * Default implementation of {@link #equals(Object)} contract for this interface. Implementations of this interface
     * are required to defer to this method to get consistent equality results across all implementations.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the reference object
     * @throws NullPointerException of {@code obj} is {@code null}
     */
    boolean bindingEquals(@NonNull T obj);

    @Override
    @NonNull String toString();

    @NonNull String bindingToString();
}
