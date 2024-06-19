/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base interface for all interfaces generated to capture a specific contract. There are five basic contracts defined
 * by YANG statements:
 * <ul>
 *   <li>{@code feature}, represented by {@link YangFeature}</li>
 *   <li>{@code identity}, represented by {@link BaseIdentity}</li>
 *   <li>{@code action}, represented by {@link Action}</li>
 *   <li>{@code rpc}, presented by {@link Rpc}</li>
 *   <li>data definition statements expressing data and metadata exchanged between two parties, represeented by
 *       {@link DataContainer}</li>
 * </ul>
 *
 * <p>
 * As can be seen, this contract is more general than {@link BindingObject}.
 *
 * @param <T> Type of the captured contract
 */
@Beta
public sealed interface BindingContract<T extends BindingContract<T>>
        permits Action, BaseIdentity, DataContainer, Rpc, YangFeature {
    /**
     * Return the interface implemented by this object. This method differs from {@link Object#getClass()} in that it
     * returns the interface contract, not a concrete implementation class.
     *
     * @return Implemented contract
     */
    @NonNull Class<? extends T> implementedInterface();
}
