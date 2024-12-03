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
public record PositiveCharacterClassExpression(CharacterGroup charGroup) implements CharacterClassExpression {
    public PositiveCharacterClassExpression {
        requireNonNull(charGroup);
    }

    @Override
    public NegativeCharacterClassExpression negate() {
        return new NegativeCharacterClassExpression(charGroup);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        charGroup.appendPatternFragment(sb.append('['));
        sb.append(']');
    }
}