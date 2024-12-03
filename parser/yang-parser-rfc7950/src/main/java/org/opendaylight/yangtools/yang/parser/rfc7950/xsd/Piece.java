/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public record Piece(Atom atom, @Nullable Quantifier quantifier) implements PatternFragment {
    public Piece {
        requireNonNull(atom);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        atom.appendPatternFragment(sb);
        final var q = quantifier;
        if (q != null) {
            q.appendPatternFragment(sb);
        }
    }
}
