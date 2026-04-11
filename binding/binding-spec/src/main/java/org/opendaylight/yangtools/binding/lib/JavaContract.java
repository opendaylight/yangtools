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
 * {@link #toString()} where that contract is defined for a particular {@link BindingContract} type.
 *
 * @param <B> base {@link BindingContract} type
 * @param <T> concrete type
 * @since 16.0.0
 */
public sealed interface JavaContract<B extends BindingContract<B>, T extends B> extends ImplementedInterface<B>
        permits JavaDataContainer {
    /**
     * Canonical implementation of Java {@link #hashCode()} contract specified by {@link #implementedInterface()}.
     *
     * @return the hash code value for this object, guaranteed to be non-{@code 0}
     */
    int bindingHashCode();

    /**
     * Canonical implementation of Java {@link #equals(Object)} contract specified by {@link #implementedInterface()}.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the reference object
     * @throws NullPointerException of {@code obj} is {@code null}
     */
    boolean bindingEquals(@NonNull T obj);

    /**
     * Canonical implementation of Java {@link #toString()} contract specified by {@link #implementedInterface()}.
     *
     * @return the string representation
     */
    @NonNull String bindingToString();

    /**
     * {@inheritDoc}
     *
     * <p>Implementations are required to delegate to {@link #bindingHashCode()}.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * <p>Implementations are required to delegate to {@link #bindingEquals(BindingContract)}.
     */
    @Override
    boolean equals(Object obj);

    /**
     * {@inheritDoc}
     *
     * <p>Implementations are required to delegate to {@link #bindingHashCode()}.
     */
    @Override
    @NonNull String toString();
}
