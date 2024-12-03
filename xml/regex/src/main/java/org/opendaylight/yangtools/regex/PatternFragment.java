/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
// TODO: Eliminate this interface once we have refactored traversal logic out of RegularExpressionParser.
//       The replacement should be a PatternFragmentGenerator.geneneratePatternFragment(String), which uses
//       the traversal logic to fill a StringBuilder internally, processing events as they occur and return its
//       resulting String.
//       That way we can realize the validation and translate-to-Pattern functionality YANG parser needs without having
//       to go through the object model -- saving a ton of allocations and CPU cycles
public sealed interface PatternFragment
        permits Atom, Branch, CharacterProperty, Piece, Quantifier, CharacterGroup, RegularExpression {

    default String toPatternFragment() {
        final var sb = new StringBuilder();
        appendPatternFragment(sb);
        return sb.toString();
    }

    void appendPatternFragment(StringBuilder sb);
}
