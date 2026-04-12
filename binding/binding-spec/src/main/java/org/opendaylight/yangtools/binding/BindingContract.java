/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.lib.ImplementedInterface;

/**
 * Base interface for all interfaces generated to capture a specific contract. There are five basic contracts defined
 * by YANG statements:
 * <ul>
 *   <li>{@code action}, represented by {@link Action}</li>
 *   <li>{@code anydata} and {@code anyxml}, represented by {@link OpaqueObject}</li>
 *   <li>{@code feature}, represented by {@link YangFeature}</li>
 *   <li>{@code identity}, represented by {@link BaseIdentity}</li>
 *   <li>{@code rpc}, represented by {@link Rpc}</li>
 *   <li>structured data and metadata exchanged between two parties, represented by {@link DataContainer}</li>
 * </ul>
 *
 * <p>As can be seen, this contract is more general than {@link BindingObject}. The exact contract is communicated via
 * {@link #implementedInterface()}, with subclasses sharpening its return value to their particular needs.
 *
 * @param <T> Type of the captured contract
 */
public sealed interface BindingContract<T extends BindingContract<T>> extends ImplementedInterface<T>
        permits Action, BaseIdentity, DataContainer, OpaqueObject, Rpc, YangFeature {
    @Override
    Class<? extends T> implementedInterface();
}
