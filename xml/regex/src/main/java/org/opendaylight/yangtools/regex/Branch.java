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
public record Branch(List<Piece> pieces) implements PatternFragment {
    public static final Branch EMPTY = new Branch(List.of());

    public Branch {
        pieces = List.copyOf(pieces);
    }

    public Branch(final Piece piece) {
        this(List.of(piece));
    }

    public Branch(final Piece... pieces) {
        this(List.of(pieces));
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        for (var piece : pieces) {
            piece.appendPatternFragment(sb);
        }
    }
}
