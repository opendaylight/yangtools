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
 * Common interface for {@code min-elements} and {@code max-elememnts} constraints.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface ElementsConstraint extends Immutable permits MaxElementsArgument {
    /**
     * {@return {@code true} if {@code elementCount} matches this constraint}
     * @param elementCount the element count
     */
    boolean matches(int elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this constraint}
     * @param elementCout the element count
     */
    boolean matches(long elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this constraint}
     * @param elementCount the element count
     */
    boolean matches(BigInteger elementCount);
}
