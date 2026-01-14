/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;

@NonNullByDefault
record ElementCountRange(MinElementsArgument minElements, Bounded maxElements) implements ElementCountMatcher {
    ElementCountRange {
        requireNonNull(minElements);
        requireNonNull(maxElements);
    }

    @Override
    public boolean match(final int elementCount) {
        return minElements.match(elementCount) && maxElements.match(elementCount);
    }

    @Override
    public boolean match(final long elementCount) {
        return minElements.match(elementCount) && maxElements.match(elementCount);
    }

    @Override
    public boolean match(final BigInteger elementCount) {
        return minElements.match(elementCount) && maxElements.match(elementCount);
    }
}
