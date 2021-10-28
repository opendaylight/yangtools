/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.concepts.Immutable;

abstract class AbstractDataTreePath<T extends AbstractDataTreeStep<?>> implements Immutable {
    private final ImmutableList<T> steps;

    AbstractDataTreePath(final ImmutableList<T> steps) {
        this.steps = requireNonNull(steps);
        checkArgument(!steps.isEmpty());
    }

    public final ImmutableList<T> steps() {
        return steps;
    }
}
