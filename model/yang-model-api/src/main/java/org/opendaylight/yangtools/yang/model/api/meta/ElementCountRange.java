/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record ElementCountRange(MinElements lower, MaxElements upper) implements ElementCountMatcher.Range {
    ElementCountRange {
        requireNonNull(lower);
        requireNonNull(upper);
    }

    @Override
    public String toString() {
        return "[" + lower + ".." + upper + "]";
    }
}
