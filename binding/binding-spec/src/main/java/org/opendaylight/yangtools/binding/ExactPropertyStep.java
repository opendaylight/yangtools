/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * An exact PropertyStep, usable in a {@link BindingInstanceIdentifier}.
 *
 * @param <C> containing {@link DataContainer} type
 * @param <V> value type
 */
public sealed interface ExactPropertyStep<C extends DataContainer, V>
    extends PropertyStep<C, V>, BindingInstanceIdentifier.Step permits LeafPropertyStep, LeafListPropertyStep {
    // Nothing else
}
