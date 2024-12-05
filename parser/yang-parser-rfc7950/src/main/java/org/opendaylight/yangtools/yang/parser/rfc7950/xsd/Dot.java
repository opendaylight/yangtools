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
public final class Dot implements CharacterClass {
    public static final Dot INSTANCE = new Dot();

    private Dot() {
        // Hidden on purpose
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        sb.append('.');
    }
}