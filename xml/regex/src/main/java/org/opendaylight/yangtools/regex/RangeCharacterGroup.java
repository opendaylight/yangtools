/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record RangeCharacterGroup(String start, String end) implements CharacterGroup {
    public RangeCharacterGroup {
        requireNonNull(start);
        requireNonNull(end);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        sb.append(start).append('-').append(end);
    }
}
