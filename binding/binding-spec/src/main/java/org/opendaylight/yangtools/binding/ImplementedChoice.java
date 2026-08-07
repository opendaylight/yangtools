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
 * @since 16.0.0
 */
sealed interface ImplementedChoice<P extends DataContainer> permits CaseObject, ChoiceIn {
    /**
     * {@return the concrete {@link ChoiceIn} class}
     */
    Class<? extends ChoiceIn<P, ?>> implementedChoice();

    /**
     * {@return the concrete {@link CaseObject} class}
     */
    Class<? extends CaseObject<P, ?, ?>> implementedCase();
}
