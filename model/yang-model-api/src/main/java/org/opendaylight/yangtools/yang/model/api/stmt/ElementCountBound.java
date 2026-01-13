/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An inclusive bound on the number of elements, as expressed via {@code min-elements} and {@code max-elememnts}
 * statements. The two represent inclusive lower and upper bound on an interval of allowed element count.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface ElementCountBound extends Immutable permits MaxElementsArgument {
    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(int elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(long elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(BigInteger elementCount);
}
