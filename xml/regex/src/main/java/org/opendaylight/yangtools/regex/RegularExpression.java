/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record RegularExpression(List<Branch> branches) implements PatternFragment {
    public RegularExpression {
        branches = List.copyOf(branches);
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("empty branches");
        }
    }

    public RegularExpression(final Branch branch) {
        this(List.of(branch));
    }

    public RegularExpression(final Branch... branches) {
        this(List.of(branches));
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        final var it = branches.iterator();
        it.next().appendPatternFragment(sb);
        it.forEachRemaining(branch -> branch.appendPatternFragment(sb.append('|')));
    }
}
