/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Metadata about a property of a {@link DataContainer}.
 *
 * @param <C> {@link DataContainer} type
 * @param <V> property value type
 * @param containerType the {@link DataContainer} class
 * @param valueType the value class
 * @param propertyName the Binding property name
 * @param yangIdentifier the YANG {@code identifier} of the statement
 */
public sealed interface PropertyMeta<C extends DataContainer, V> permits LeafPropertyMeta, LeafListPropertyMeta {

    @NonNull Class<C> containerType();

    @NonNull Class<V> valueType();

    @NonNull String propertyName();

    @NonNull Unqualified yangIdentifier();
}
