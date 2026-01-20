/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An {@link Immutable} {@link Iterable} which exposes its {@link #size()}.
 *
 * @since 15.0.0
 */
@Beta
@NonNullByDefault
public sealed interface SizedIterable<T> extends Iterable<T>, Immutable permits KeyArgument, ValueRanges {
    /**
     * {@return the number of items contained in this iterable, guaranteed to be positive}
     */
    int size();
}
