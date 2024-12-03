/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public sealed interface PatternFragment
        permits Atom, Branch, CharacterProperty, Piece, Quantifier, CharacterGroup, RegularExpression {

    default String toPatternFragment() {
        final var sb = new StringBuilder();
        appendPatternFragment(sb);
        return sb.toString();
    }

    void appendPatternFragment(StringBuilder sb);
}
