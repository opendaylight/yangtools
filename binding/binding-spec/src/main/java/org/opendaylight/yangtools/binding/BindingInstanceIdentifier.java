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
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Binding representation of a {@code instance-identifier}.
 */
public sealed interface BindingInstanceIdentifier extends Immutable, PathLike, Serializable
        permits DataObjectIdentifier, PropertyIdentifier {
    /**
     * A single step in a {@link BindingInstanceIdentifier}.
     */
    sealed interface Step extends Serializable permits ExactDataObjectStep, ExactPropertyStep {
        // Nothing else
    }

    /**
     * Return the individual steps of this identifier. Returned {@link Iterable} does not support removals and contains
     * one or more non-null items.
     *
     * @return the individual steps of this identifier.
     */
    Iterable<? extends @NonNull Step> steps();
}
