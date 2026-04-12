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
 * {@link BindingContract}'s sole method, {@link #implementedInterface()}. This interface exists only to improve
 * coupling between {@link BindingContract} and generated code for contract specializations: we want the implementation
 * library code to provide useful integration, but we do not want it to clutter  {@link BindingContract} type hierarchy.
 *
 * <p>This makes this interface has two views:
 * <ul>
 *   <li>{@link BindingContract} is the user-facing view, which is customized through generated contracts</li>
 *   <li>{@link JavaContract} is the implementation-facing view</li>
 * </ul>
 *
 * @since 15.1.0
 */
public sealed interface ImplementedInterface<T extends BindingContract<T>> permits BindingContract, JavaContract {
    /**
     * Return the interface implemented by this object. This method differs from {@link Object#getClass()} in that it
     * returns the interface contract, not a concrete implementation class.
     *
     * @return Implemented contract
     */
    @NonNull Class<? extends T> implementedInterface();
}
