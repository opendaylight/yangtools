/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * A {@link BindingInstanceIdentifier.Step} which matches a non-DataContainer property of a {@link DataContainer}. These
 * correspond to getter methods generated in a {@link DataContainer} for {@code leaf} and {@code leaf-list} statements.
 *
 * @param <C> containing {@link DataContainer} type
 * @param <V> value type
 */
public sealed interface PropertyStep<C extends DataContainer, V> extends Serializable permits ExactPropertyStep {
    /**
     * Returns a class reference to containing {@link DataContainer}.
     *
     * @return a class reference to containing {@link DataContainer}
     */
    @NonNull Class<C> containerType();

    /**
     * Returns the value type.
     *
     * @return the value type
     */
    @NonNull Class<V> valueType();

    /**
     * Returns the YANG {@code identifier} argument of the statement.
     *
     * @return the YANG {@code identifier} argument of the statement
     */
    @NonNull Unqualified yangIdentifier();
}
