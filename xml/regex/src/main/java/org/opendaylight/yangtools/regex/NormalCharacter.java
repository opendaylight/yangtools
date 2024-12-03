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

// FIXME: We use String here, which is quite wasteful. We really want to capture a Unicode code point here with an easy
//        interop with String: we want to encode the code point in terms of a surrogate pair, each encoded as a 'char'.
//        Since we are interfacing a well-formed String, we do not need to encode unpaired surrogates and should thrown
//        an Exception when the first byte of String is a a surrogate. This also means we can trivially discern when
//        we are dealing with a surrogate pair vs. a single char.
@NonNullByDefault
public record NormalCharacter(String str) implements Atom {
    public NormalCharacter {
        requireNonNull(str);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        sb.append(str);
    }
}
