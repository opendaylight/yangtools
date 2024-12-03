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
public record DifferenceCharacterGroup(CharacterGroup left, CharacterClassExpression right) implements CharacterGroup {
    public DifferenceCharacterGroup {
        requireNonNull(left);
        requireNonNull(right);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        left.appendPatternFragment(sb);
        right.negate().appendPatternFragment(sb.append("&&"));
    }
}
