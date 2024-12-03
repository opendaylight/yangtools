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
public record ParenRegularExpression(RegularExpression regex) implements Atom {
    public ParenRegularExpression {
        requireNonNull(regex);
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        // (?:X) instead of (X) because Pattern groups are capturing by default. XSD does not have back-references,
        // there is no point in capturing them.
        regex.appendPatternFragment(sb.append("(?:"));
        sb.append(')');
    }
}
