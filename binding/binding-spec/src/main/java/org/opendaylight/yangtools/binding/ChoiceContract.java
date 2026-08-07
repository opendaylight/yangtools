/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * Common glue interface between {@link CaseObject} and {@link ChoiceIn} to enforce consistency.
 *
 * @param <P> Parent {@link DataContainer}
 * @param <C> a {@link ChoiceIn} in that parent
 * @since 16.0.0
 */
public sealed interface ChoiceContract<P extends DataContainer, C extends ChoiceIn<P, ?>> permits CaseObject, ChoiceIn {
    /**
     * {@return the concrete {@link ChoiceIn} class}
     */
    Class<? extends C> implementedChoice();

    /**
     * {@return the concrete {@link CaseObject} class}
     */
    Class<? extends CaseObject<P, ? super C, ?>> implementedCase();
}
